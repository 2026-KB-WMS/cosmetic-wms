package com.kb.cosmetic_wms.integration;

import com.kb.cosmetic_wms.inventory.application.port.in.FefoInventorySlice;
import com.kb.cosmetic_wms.inventory.application.port.in.FindFefoInventoryUseCase;
import com.kb.cosmetic_wms.inventory.application.port.in.FindProductAvailabilityUseCase;
import com.kb.cosmetic_wms.inventory.application.port.in.InventoryStatusChangeCommand;
import com.kb.cosmetic_wms.inventory.application.port.in.ManageInventoryStatusUseCase;
import com.kb.cosmetic_wms.inventory.application.port.out.LotBlockPort;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryLotBlockedException;
import com.kb.cosmetic_wms.lot.application.port.in.UpdateLotStatusCommand;
import com.kb.cosmetic_wms.lot.application.port.in.UpdateLotStatusUseCase;
import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import com.kb.cosmetic_wms.support.MySqlContainerSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@TestPropertySource(properties = "spring.datasource.hikari.connection-init-sql=SET FOREIGN_KEY_CHECKS=0")
class LotStatusInventoryBlockIntegrationTest extends MySqlContainerSupport {

    private static final long PRODUCT_ID = 9701L;
    private static final long WAREHOUSE_ID = 9801L;
    private static final long INBOUND_ID_BASE = 9600L;

    @Autowired
    private UpdateLotStatusUseCase updateLotStatusUseCase;

    @Autowired
    private FindFefoInventoryUseCase findFefoInventoryUseCase;

    @Autowired
    private FindProductAvailabilityUseCase findProductAvailabilityUseCase;

    @Autowired
    private ManageInventoryStatusUseCase manageInventoryStatusUseCase;

    @MockitoSpyBean
    private LotBlockPort lotBlockPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    private long sectionSeq = 970000;
    private long lotSeq;

    @BeforeEach
    void setUp() {
        cleanUp();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        jdbcTemplate.update("DELETE FROM inventory WHERE product_id = ?", PRODUCT_ID);
        jdbcTemplate.update("DELETE FROM inventory_lot_block WHERE lot_id IN (SELECT lot_id FROM lot WHERE inbound_id BETWEEN ? AND ?)",
                INBOUND_ID_BASE, INBOUND_ID_BASE + 100);
        jdbcTemplate.update("DELETE FROM lot WHERE inbound_id BETWEEN ? AND ?", INBOUND_ID_BASE, INBOUND_ID_BASE + 100);
    }

    private long insertLot() {
        long inboundId = INBOUND_ID_BASE + (++lotSeq);
        jdbcTemplate.update("""
                INSERT INTO lot (inbound_id, manufacturer_lot_no, manufacturing_date, expiration_date, status,
                                 product_id, created_by, created_at)
                VALUES (?, ?, '2026-01-01', '2030-01-01', 'AVAILABLE', ?, 0, NOW())
                """, inboundId, "BLK-" + lotSeq, PRODUCT_ID);
        return jdbcTemplate.queryForObject("SELECT lot_id FROM lot WHERE inbound_id = ?", Long.class, inboundId);
    }

    private long insertInventory(long lotId, int quantity, int expiryDays, String allocStatus, String qualityStatus) {
        jdbcTemplate.update("""
                INSERT INTO inventory (product_id, lot_id, section_id, warehouse_id, quantity, available_quantity,
                                       alloc_status, quality_status, loc_status, expiry_date, created_by, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'STORED', ?, 0, NOW())
                """, PRODUCT_ID, lotId, ++sectionSeq, WAREHOUSE_ID, quantity, quantity, allocStatus, qualityStatus,
                LocalDate.now(clock).plusDays(expiryDays));
        return jdbcTemplate.queryForObject("SELECT MAX(inventory_id) FROM inventory WHERE product_id = ?",
                Long.class, PRODUCT_ID);
    }

    private List<Long> fefoInventoryIds() {
        return findFefoInventoryUseCase.findAvailableForFefo(PRODUCT_ID, WAREHOUSE_ID).stream()
                .map(FefoInventorySlice::inventoryId).toList();
    }

    private void changeLotStatus(long lotId, LotStatus status) {
        updateLotStatusUseCase.updateStatus(lotId, new UpdateLotStatusCommand(status));
    }

    private String blockStatus(long lotId) {
        List<String> rows = jdbcTemplate.queryForList(
                "SELECT status FROM inventory_lot_block WHERE lot_id = ?", String.class, lotId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Test
    void 로트를_리콜하면_차단_목록에_사유와_함께_반영되고_그_로트의_재고는_FEFO_후보에서_빠진다() {
        long recalled = insertLot();
        long normal = insertLot();
        long recalledInv = insertInventory(recalled, 50, 20, "UNALLOCATED", "NORMAL");
        long normalInv = insertInventory(normal, 30, 60, "UNALLOCATED", "NORMAL");
        assertThat(fefoInventoryIds()).containsExactly(recalledInv, normalInv);

        changeLotStatus(recalled, LotStatus.RECALLED);

        assertThat(blockStatus(recalled)).isEqualTo("RECALLED");
        assertThat(fefoInventoryIds()).containsExactly(normalInv);
        assertThat(findProductAvailabilityUseCase.findAvailabilityByProducts(List.of(PRODUCT_ID)))
                .singleElement().satisfies(slice -> assertThat(slice.availableQuantity()).isEqualTo(30));
    }

    @Test
    void 보류했다가_정상으로_복구하면_차단이_해제되어_다시_후보가_된다() {
        long lot = insertLot();
        long inv = insertInventory(lot, 50, 60, "UNALLOCATED", "NORMAL");

        changeLotStatus(lot, LotStatus.HOLD);
        assertThat(blockStatus(lot)).isEqualTo("HOLD");
        assertThat(fefoInventoryIds()).isEmpty();

        changeLotStatus(lot, LotStatus.AVAILABLE);
        assertThat(blockStatus(lot)).isNull();
        assertThat(fefoInventoryIds()).containsExactly(inv);
    }

    @Test
    void 보류에서_리콜로_바뀌면_차단은_유지되고_사유만_갱신된다() {
        long lot = insertLot();
        insertInventory(lot, 50, 60, "UNALLOCATED", "NORMAL");

        changeLotStatus(lot, LotStatus.HOLD);
        changeLotStatus(lot, LotStatus.RECALLED);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_lot_block WHERE lot_id = ?", Integer.class, lot)).isEqualTo(1);
        assertThat(blockStatus(lot)).isEqualTo("RECALLED");
        assertThat(fefoInventoryIds()).isEmpty();
    }

    @Test
    void 검사_불합격으로_HOLD인_재고는_로트를_복구해도_HOLD로_남고_후보가_되지_않는다() {
        // given — 같은 로트에 정상 재고와 검사 불합격 재고
        long lot = insertLot();
        long normalInv = insertInventory(lot, 40, 60, "UNALLOCATED", "NORMAL");
        long failedInv = insertInventory(lot, 10, 60, "UNALLOCATED", "HOLD");

        // when — 로트를 보류했다가 복구
        changeLotStatus(lot, LotStatus.HOLD);
        changeLotStatus(lot, LotStatus.AVAILABLE);

        // then — 복구는 차단 목록만 되돌리고 재고의 품질 상태는 건드리지 않는다
        assertThat(jdbcTemplate.queryForObject(
                "SELECT quality_status FROM inventory WHERE inventory_id = ?", String.class, failedInv)).isEqualTo("HOLD");
        assertThat(fefoInventoryIds()).containsExactly(normalInv);
    }

    @Test
    void 차단된_뒤에_같은_로트에_새로_생긴_재고도_후보가_되지_않는다() {
        long lot = insertLot();
        changeLotStatus(lot, LotStatus.RECALLED);

        insertInventory(lot, 50, 60, "UNALLOCATED", "NORMAL");

        assertThat(fefoInventoryIds()).isEmpty();
    }

    @Test
    void 차단된_로트의_재고를_수동으로_할당하면_거부되고_재고는_변하지_않는다() {
        long lot = insertLot();
        long inv = insertInventory(lot, 50, 60, "UNALLOCATED", "NORMAL");
        changeLotStatus(lot, LotStatus.RECALLED);

        assertThatThrownBy(() -> manageInventoryStatusUseCase.allocate(inv,
                new InventoryStatusChangeCommand(10, 1L, 1L)))
                .isInstanceOf(InventoryLotBlockedException.class);

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT alloc_status, quantity FROM inventory WHERE inventory_id = ?", inv);
        assertThat(row.get("alloc_status")).isEqualTo("UNALLOCATED");
        assertThat(((Number) row.get("quantity")).intValue()).isEqualTo(50);
    }

    @Test
    void 이미_할당된_재고는_로트를_리콜해도_자동으로_바뀌지_않는다() {
        long lot = insertLot();
        long allocated = insertInventory(lot, 50, 60, "ALLOCATED", "NORMAL");

        changeLotStatus(lot, LotStatus.RECALLED);

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT alloc_status, quantity FROM inventory WHERE inventory_id = ?", allocated);
        assertThat(row.get("alloc_status")).isEqualTo("ALLOCATED");
        assertThat(((Number) row.get("quantity")).intValue()).isEqualTo(50);
    }

    @Test
    void 차단_목록_갱신이_실패하면_로트_상태_변경도_함께_롤백된다() {
        long lot = insertLot();
        doThrow(new IllegalStateException("차단 목록 갱신 실패")).when(lotBlockPort).block(anyLong(), anyString());

        assertThatThrownBy(() -> changeLotStatus(lot, LotStatus.RECALLED)).isInstanceOf(RuntimeException.class);

        assertThat(jdbcTemplate.queryForObject("SELECT status FROM lot WHERE lot_id = ?", String.class, lot))
                .isEqualTo("AVAILABLE");
        assertThat(blockStatus(lot)).isNull();
    }
}
