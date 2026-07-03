package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

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
class InventoryAvailabilityRepositoryTest {

    @Autowired
    private InventoryJpaRepository inventoryJpaRepository;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        // 집계 쿼리 검증이 목적이므로 마스터 데이터(FK 체인) 세팅 대신 참조 무결성을 해제한다
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM inventory");
    }

    @Test
    void 창고와_상품별로_가용_수량_합계와_최근접_유통기한을_집계한다() {
        // given — 창고 1에 상품 100 재고 2건 (유통기한 상이)
        insertInventory(1L, 100L, 30, "2026-09-01", "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 20, "2026-08-01", "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(2L, 100L, 50, "2026-10-01", "UNALLOCATED", "NORMAL", "STORED");

        // when
        List<Object[]> rows = inventoryJpaRepository.findAvailabilityByProducts(List.of(100L));

        // then — (창고1, 상품100) 합계 50 / MIN 2026-08-01, (창고2, 상품100) 합계 50
        assertThat(rows).hasSize(2);
        Object[] wh1 = findRow(rows, 1L);
        assertThat(((Number) wh1[2]).intValue()).isEqualTo(50);
        assertThat(toLocalDate(wh1[3])).isEqualTo(LocalDate.of(2026, 8, 1));

        Object[] wh2 = findRow(rows, 2L);
        assertThat(((Number) wh2[2]).intValue()).isEqualTo(50);
    }

    @Test
    void 할당됐거나_비정상이거나_보관_전인_재고는_집계에서_제외된다() {
        // given — 유효 재고 1건 + 상태 조건 위반 3건
        insertInventory(1L, 100L, 10, "2026-09-01", "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 99, "2026-01-01", "ALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 100L, 99, "2026-01-01", "UNALLOCATED", "HOLD", "STORED");
        insertInventory(1L, 100L, 99, "2026-01-01", "UNALLOCATED", "NORMAL", "IN_TRANSIT");

        // when
        List<Object[]> rows = inventoryJpaRepository.findAvailabilityByProducts(List.of(100L));

        // then
        assertThat(rows).hasSize(1);
        assertThat(((Number) rows.getFirst()[2]).intValue()).isEqualTo(10);
        assertThat(toLocalDate(rows.getFirst()[3])).isEqualTo(LocalDate.of(2026, 9, 1));
    }

    @Test
    void 요청하지_않은_상품의_재고는_집계되지_않는다() {
        // given
        insertInventory(1L, 100L, 10, "2026-09-01", "UNALLOCATED", "NORMAL", "STORED");
        insertInventory(1L, 200L, 10, "2026-09-01", "UNALLOCATED", "NORMAL", "STORED");

        // when
        List<Object[]> rows = inventoryJpaRepository.findAvailabilityByProducts(List.of(100L));

        // then
        assertThat(rows).hasSize(1);
        assertThat(((Number) rows.getFirst()[1]).longValue()).isEqualTo(100L);
    }

    private long sectionSeq = 1;

    private void insertInventory(Long warehouseId, Long productId, int availableQty, String expiryDate,
                                 String allocStatus, String qualityStatus, String locStatus) {
        jdbcTemplate.update("""
                        INSERT INTO inventory (product_id, lot_id, section_id, warehouse_id, quantity,
                                               available_quantity, alloc_status, quality_status, loc_status,
                                               expiry_date, created_by, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, NOW())
                        """,
                productId, 1L, sectionSeq++, warehouseId, availableQty,
                availableQty, allocStatus, qualityStatus, locStatus, expiryDate);
    }

    private static Object[] findRow(List<Object[]> rows, Long warehouseId) {
        return rows.stream()
                .filter(r -> ((Number) r[0]).longValue() == warehouseId)
                .findFirst()
                .orElseThrow();
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        return (LocalDate) value;
    }
}
