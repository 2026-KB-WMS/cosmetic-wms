package com.kb.cosmetic_wms.integration;

import com.kb.cosmetic_wms.inventory.application.port.in.InventoryStatusChangeCommand;
import com.kb.cosmetic_wms.inventory.application.port.in.ManageInventoryStatusUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 재고 상태 변경이 DB까지 반영되는지 검증하는 회귀 테스트.
 *
 * 도메인 모델(Inventory)은 영속성 어댑터에서 detached POJO로 매핑되므로
 * JPA dirty checking이 동작하지 않는다. 분할·병합·상태 변경 후 변경된 객체를
 * 명시적으로 save 하지 않으면 변경분이 조용히 유실된다(과거 재고 부풀림 버그).
 */
@SpringBootTest
class InventoryAllocationPersistenceIntegrationTest {

    private static final Long PRODUCT_ID = 900L;

    @Autowired
    private ManageInventoryStatusUseCase manageInventoryStatusUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        cleanUp();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        jdbcTemplate.update("DELETE FROM inventory_transaction");
        jdbcTemplate.update("DELETE FROM inventory WHERE product_id = ?", PRODUCT_ID);
    }

    @Test
    void 부분_할당하면_원본_재고_행의_차감이_DB에_반영되고_전체_수량이_보존된다() {
        // given
        Long inventoryId = insertInventory(100);

        // when
        manageInventoryStatusUseCase.allocate(inventoryId,
                new InventoryStatusChangeCommand(30, 1L, 1L));

        // then
        assertThat(quantityOf(inventoryId)).isEqualTo(70);
        assertThat(totalQuantity()).isEqualTo(100);
        assertThat(allocatedQuantity()).isEqualTo(30);
    }

    @Test
    void 전량_할당하면_상태_변경이_DB에_반영된다() {
        // given
        Long inventoryId = insertInventory(30);

        // when
        manageInventoryStatusUseCase.allocate(inventoryId,
                new InventoryStatusChangeCommand(30, 1L, 1L));

        // then
        String allocStatus = jdbcTemplate.queryForObject(
                "SELECT alloc_status FROM inventory WHERE inventory_id = ?", String.class, inventoryId);
        assertThat(allocStatus).isEqualTo("ALLOCATED");
    }

    @Test
    void 동일_상태_재고가_이미_존재하면_합산_결과가_DB에_반영되고_전체_수량이_보존된다() {
        // given — 첫 할당으로 ALLOCATED 분할 행 생성
        Long inventoryId = insertInventory(100);
        manageInventoryStatusUseCase.allocate(inventoryId,
                new InventoryStatusChangeCommand(30, 1L, 1L));

        // when — 두 번째 할당은 기존 ALLOCATED 행에 병합
        manageInventoryStatusUseCase.allocate(inventoryId,
                new InventoryStatusChangeCommand(20, 1L, 1L));

        // then
        assertThat(quantityOf(inventoryId)).isEqualTo(50);
        assertThat(allocatedQuantity()).isEqualTo(50);
        assertThat(totalQuantity()).isEqualTo(100);
        Integer rowCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory WHERE product_id = ?", Integer.class, PRODUCT_ID);
        assertThat(rowCount).isEqualTo(2);
    }

    private Long insertInventory(int quantity) {
        jdbcTemplate.update("""
                        INSERT INTO inventory (product_id, lot_id, section_id, warehouse_id, quantity,
                                               available_quantity, alloc_status, quality_status, loc_status,
                                               expiry_date, created_by, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, 'UNALLOCATED', 'NORMAL', 'STORED', ?, 1, NOW(), NOW())
                        """,
                PRODUCT_ID, 900L, 900L, 900L, quantity, quantity, "2026-12-31");
        return jdbcTemplate.queryForObject(
                "SELECT inventory_id FROM inventory WHERE product_id = ?", Long.class, PRODUCT_ID);
    }

    private Integer quantityOf(Long inventoryId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantity FROM inventory WHERE inventory_id = ?", Integer.class, inventoryId);
    }

    private Integer totalQuantity() {
        return jdbcTemplate.queryForObject(
                "SELECT SUM(quantity) FROM inventory WHERE product_id = ?", Integer.class, PRODUCT_ID);
    }

    private Integer allocatedQuantity() {
        return jdbcTemplate.queryForObject(
                "SELECT SUM(quantity) FROM inventory WHERE product_id = ? AND alloc_status = 'ALLOCATED'",
                Integer.class, PRODUCT_ID);
    }
}
