package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import org.springframework.test.context.TestPropertySource;
import com.kb.cosmetic_wms.support.MySqlContainerSupport;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.kb.cosmetic_wms.inventory.adapter.out.persistence.InventoryJpaRepository.FefoInventoryProjection;
import com.kb.cosmetic_wms.inventory.adapter.out.persistence.InventoryJpaRepository.ProductAvailabilityProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.datasource.hikari.connection-init-sql=SET FOREIGN_KEY_CHECKS=0")
class InventoryAvailabilityRepositoryTest extends MySqlContainerSupport {

    @Autowired
    private InventoryJpaRepository inventoryJpaRepository;

    @Autowired
    private DataSource dataSource;

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate ALLOCATABLE_AFTER = TODAY.plusDays(7);

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        // 집계 쿼리 검증이 목적이므로 마스터 데이터(FK 체인) 세팅 대신 클래스의 FOREIGN_KEY_CHECKS=0 설정으로 참조 무결성을 해제한다
        jdbcTemplate.execute("DELETE FROM inventory");
        jdbcTemplate.execute("DELETE FROM inventory_lot_block");
    }

    @Test
    void 창고와_상품별로_가용_수량_합계와_최근접_유통기한을_집계한다() {
        // given — 창고 1에 상품 100 재고 2건 (유통기한 상이)
        insertInventory(1L, 100L, 30, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 20, TODAY.plusDays(30), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(2L, 100L, 50, TODAY.plusDays(90), "UNALLOCATED", "NORMAL", "STORED");

        // when
        List<ProductAvailabilityProjection> rows =
                inventoryJpaRepository.findAvailabilityByProducts(List.of(100L), ALLOCATABLE_AFTER);

        // then — (창고1, 상품100) 합계 50 / MIN 30일 뒤, (창고2, 상품100) 합계 50
        assertThat(rows).hasSize(2);
        ProductAvailabilityProjection wh1 = findRow(rows, 1L);
        assertThat(wh1.getAvailableQuantity()).isEqualTo(50);
        assertThat(wh1.getEarliestExpiryDate()).isEqualTo(TODAY.plusDays(30));

        ProductAvailabilityProjection wh2 = findRow(rows, 2L);
        assertThat(wh2.getAvailableQuantity()).isEqualTo(50);
    }

    @Test
    void 할당됐거나_비정상이거나_보관_전인_재고는_집계에서_제외된다() {
        // given — 유효 재고 1건 + 상태 조건 위반 3건
        insertInventory(1L, 100L, 10, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 99, TODAY.plusDays(60), "ALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 99, TODAY.plusDays(60), "UNALLOCATED", "HOLD", "STORED");
        insertInventory(1L, 100L, 99, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "IN_TRANSIT");

        // when
        List<ProductAvailabilityProjection> rows =
                inventoryJpaRepository.findAvailabilityByProducts(List.of(100L), ALLOCATABLE_AFTER);

        // then
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getAvailableQuantity()).isEqualTo(10);
        assertThat(rows.getFirst().getEarliestExpiryDate()).isEqualTo(TODAY.plusDays(60));
    }

    @Test
    void 요청하지_않은_상품의_재고는_집계되지_않는다() {
        // given
        insertInventory(1L, 100L, 10, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 200L, 10, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");

        // when
        List<ProductAvailabilityProjection> rows =
                inventoryJpaRepository.findAvailabilityByProducts(List.of(100L), ALLOCATABLE_AFTER);

        // then
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getProductId()).isEqualTo(100L);
    }

    @Test
    void 만료되었거나_잔여_7일_이하인_재고는_가용_재고_집계에서_제외된다() {
        // given — 만료, 오늘 만료, 잔여 7일, 잔여 8일(포함), 기본값(만료일 없음)
        insertInventory(1L, 100L, 1, TODAY.minusDays(30), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 2, TODAY, "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 4, TODAY.plusDays(7), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 8, TODAY.plusDays(8), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 16, LocalDate.of(9999, 12, 31), "UNALLOCATED", "NORMAL", "STORED");

        // when
        List<ProductAvailabilityProjection> rows =
                inventoryJpaRepository.findAvailabilityByProducts(List.of(100L), ALLOCATABLE_AFTER);

        // then — 잔여 8일(8)과 만료일 없음(16)만 센다
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getAvailableQuantity()).isEqualTo(24);
        assertThat(rows.getFirst().getEarliestExpiryDate()).isEqualTo(TODAY.plusDays(8));
    }

    @Test
    void 임박_재고만_있는_창고는_가용_재고_집계에_나타나지_않는다() {
        // given
        insertInventory(1L, 100L, 100, TODAY.plusDays(3), "UNALLOCATED", "NORMAL", "STORED");

        // when & then
        assertThat(inventoryJpaRepository.findAvailabilityByProducts(List.of(100L), ALLOCATABLE_AFTER)).isEmpty();
    }

    @Test
    void FEFO_후보는_유통기한이_빠른_순서이되_만료되었거나_잔여_7일_이하인_재고는_제외한다() {
        // given — 만료(먼저 나가면 안 됨), 잔여 7일(제외), 잔여 8일, 잔여 60일, 다른 창고의 재고
        insertInventory(1L, 100L, 11, TODAY.minusDays(5), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 22, TODAY.plusDays(7), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 33, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 44, TODAY.plusDays(8), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(2L, 100L, 55, TODAY.plusDays(20), "UNALLOCATED", "NORMAL", "STORED");

        // when
        List<FefoInventoryProjection> candidates =
                inventoryJpaRepository.findAvailableForFefo(100L, 1L, ALLOCATABLE_AFTER);

        // then — 잔여 8일(44) → 잔여 60일(33) 순서
        assertThat(candidates).extracting(FefoInventoryProjection::getAvailableQuantity)
                .containsExactly(44, 33);
    }

    @Test
    void FEFO_후보도_할당됐거나_비정상인_재고는_제외한다() {
        // given
        insertInventory(1L, 100L, 10, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 20, TODAY.plusDays(60), "ALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 30, TODAY.plusDays(60), "UNALLOCATED", "HOLD", "STORED");

        // when
        List<FefoInventoryProjection> candidates =
                inventoryJpaRepository.findAvailableForFefo(100L, 1L, ALLOCATABLE_AFTER);

        // then
        assertThat(candidates).extracting(FefoInventoryProjection::getAvailableQuantity).containsExactly(10);
    }

    @Test
    void 차단된_로트의_재고는_FEFO_후보에서_제외된다() {
        // given — 로트 1은 정상, 로트 77은 리콜(더 빨리 만료되는 재고)
        insertInventoryOfLot(1L, 1L, 100L, 10, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");
        insertInventoryOfLot(77L, 1L, 100L, 99, TODAY.plusDays(20), "UNALLOCATED", "NORMAL", "STORED");
        insertBlock(77L, "RECALLED");

        // when
        List<FefoInventoryProjection> candidates =
                inventoryJpaRepository.findAvailableForFefo(100L, 1L, ALLOCATABLE_AFTER);

        // then
        assertThat(candidates).extracting(FefoInventoryProjection::getAvailableQuantity).containsExactly(10);
    }

    @Test
    void 차단된_로트의_재고는_가용_재고_집계에서_제외된다() {
        // given
        insertInventoryOfLot(1L, 1L, 100L, 10, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");
        insertInventoryOfLot(77L, 1L, 100L, 99, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");
        insertBlock(77L, "HOLD");

        // when
        List<ProductAvailabilityProjection> rows =
                inventoryJpaRepository.findAvailabilityByProducts(List.of(100L), ALLOCATABLE_AFTER);

        // then
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getAvailableQuantity()).isEqualTo(10);
    }

    @Test
    void 차단된_로트에_나중에_생긴_재고도_자동으로_제외된다() {
        // given — 차단이 먼저 있고, 그 뒤에 같은 로트의 재고가 만들어진다 (검사 합격 등)
        insertBlock(77L, "RECALLED");
        insertInventoryOfLot(77L, 1L, 100L, 50, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");

        // when & then
        assertThat(inventoryJpaRepository.findAvailableForFefo(100L, 1L, ALLOCATABLE_AFTER)).isEmpty();
        assertThat(inventoryJpaRepository.findAvailabilityByProducts(List.of(100L), ALLOCATABLE_AFTER)).isEmpty();
    }

    @Test
    void 차단이_해제된_로트의_재고는_다시_후보가_된다() {
        // given
        insertInventoryOfLot(77L, 1L, 100L, 50, TODAY.plusDays(60), "UNALLOCATED", "NORMAL", "STORED");
        insertBlock(77L, "HOLD");
        assertThat(inventoryJpaRepository.findAvailableForFefo(100L, 1L, ALLOCATABLE_AFTER)).isEmpty();

        // when
        jdbcTemplate.update("DELETE FROM inventory_lot_block WHERE lot_id = ?", 77L);

        // then
        assertThat(inventoryJpaRepository.findAvailableForFefo(100L, 1L, ALLOCATABLE_AFTER)).hasSize(1);
    }

    private long sectionSeq = 1;

    private void insertInventory(Long warehouseId, Long productId, int availableQty, LocalDate expiryDate,
                                 String allocStatus, String qualityStatus, String locStatus) {
        insertInventoryOfLot(1L, warehouseId, productId, availableQty, expiryDate, allocStatus, qualityStatus, locStatus);
    }

    private void insertBlock(long lotId, String status) {
        jdbcTemplate.update("""
                INSERT INTO inventory_lot_block (lot_id, status, created_by, created_at)
                VALUES (?, ?, 1, NOW())
                """, lotId, status);
    }

    private void insertInventoryOfLot(Long lotId, Long warehouseId, Long productId, int availableQty,
                                      LocalDate expiryDate, String allocStatus, String qualityStatus,
                                      String locStatus) {
        jdbcTemplate.update("""
                        INSERT INTO inventory (product_id, lot_id, section_id, warehouse_id, quantity,
                                               available_quantity, alloc_status, quality_status, loc_status,
                                               expiry_date, created_by, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, NOW())
                        """,
                productId, lotId, sectionSeq++, warehouseId, availableQty,
                availableQty, allocStatus, qualityStatus, locStatus, expiryDate);
    }

    private static ProductAvailabilityProjection findRow(List<ProductAvailabilityProjection> rows, Long warehouseId) {
        return rows.stream()
                .filter(r -> r.getWarehouseId().equals(warehouseId))
                .findFirst()
                .orElseThrow();
    }
}
