package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import com.kb.cosmetic_wms.support.MySqlContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.datasource.hikari.connection-init-sql=SET FOREIGN_KEY_CHECKS=0")
class LotAllocationQueryRepositoryTest extends MySqlContainerSupport {

    private static final long LOT = 10L;
    private static final long OTHER_LOT = 20L;

    @Autowired
    private InventoryTransactionJpaRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long sectionSeq = 1;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM inventory_transaction");
        jdbcTemplate.update("DELETE FROM inventory");
    }

    private long insertInventory(long lotId, String allocStatus) {
        jdbcTemplate.update("""
                INSERT INTO inventory (product_id, lot_id, section_id, warehouse_id, quantity, available_quantity,
                                       alloc_status, quality_status, loc_status, expiry_date, created_by, created_at)
                VALUES (1, ?, ?, 1, 50, 0, ?, 'NORMAL', 'STORED', '2030-01-01', 0, NOW())
                """, lotId, sectionSeq++, allocStatus);
        return jdbcTemplate.queryForObject("SELECT MAX(inventory_id) FROM inventory", Long.class);
    }

    private void insertTransaction(long inventoryId, String type, Long referenceId) {
        jdbcTemplate.update("""
                INSERT INTO inventory_transaction (inventory_id, transaction_type, transaction_qty, balance_qty, reference_id,
                                                   curr_alloc_status, curr_quality_status, curr_loc_status, member_id,
                                                   created_by, created_at)
                VALUES (?, ?, 10, 50, ?, 'ALLOCATED', 'NORMAL', 'STORED', 1, 0, NOW())
                """, inventoryId, type, referenceId);
    }

    @Test
    void 로트의_할당된_재고에_대한_ALLOCATE_이력의_출고_ID를_반환한다() {
        long allocated = insertInventory(LOT, "ALLOCATED");
        insertTransaction(allocated, "ALLOCATE", 101L);

        assertThat(repository.findAllocatedReferenceIdsByLot(LOT)).containsExactly(101L);
    }

    @Test
    void 출고가_달라도_한_행으로_병합된_할당은_출고_ID를_모두_반환한다() {
        // given — 같은 로트·구역의 할당이 병합되어 행은 하나이고, 이력만 출고별로 남아 있다
        long merged = insertInventory(LOT, "ALLOCATED");
        insertTransaction(merged, "ALLOCATE", 101L);
        insertTransaction(merged, "ALLOCATE", 102L);

        assertThat(repository.findAllocatedReferenceIdsByLot(LOT)).containsExactlyInAnyOrder(101L, 102L);
    }

    @Test
    void 같은_출고의_이력이_여러_건이어도_출고_ID는_한_번만_반환한다() {
        long allocated = insertInventory(LOT, "ALLOCATED");
        insertTransaction(allocated, "ALLOCATE", 101L);
        insertTransaction(allocated, "ALLOCATE", 101L);

        assertThat(repository.findAllocatedReferenceIdsByLot(LOT)).containsExactly(101L);
    }

    @Test
    void 다른_로트의_할당은_포함하지_않는다() {
        long other = insertInventory(OTHER_LOT, "ALLOCATED");
        insertTransaction(other, "ALLOCATE", 201L);

        assertThat(repository.findAllocatedReferenceIdsByLot(LOT)).isEmpty();
    }

    @Test
    void 이미_할당이_풀린_재고의_이력은_포함하지_않는다() {
        long released = insertInventory(LOT, "UNALLOCATED");
        insertTransaction(released, "ALLOCATE", 101L);

        assertThat(repository.findAllocatedReferenceIdsByLot(LOT)).isEmpty();
    }

    @Test
    void ALLOCATE가_아닌_이력과_참조가_없는_이력은_포함하지_않는다() {
        long allocated = insertInventory(LOT, "ALLOCATED");
        insertTransaction(allocated, "SPLIT_DEDUCT", 101L);
        insertTransaction(allocated, "SHIP", 102L);
        insertTransaction(allocated, "ALLOCATE", null);

        assertThat(repository.findAllocatedReferenceIdsByLot(LOT)).isEmpty();
    }

    @Test
    void 삭제된_재고의_이력은_포함하지_않는다() {
        // 출하로 재고 행이 사라진 경우: 이력은 남아 있지만 조인되는 재고가 없다
        insertTransaction(9999L, "ALLOCATE", 101L);

        assertThat(repository.findAllocatedReferenceIdsByLot(LOT)).isEmpty();
    }

    @Test
    void 결과는_출고_ID_오름차순이다() {
        long allocated = insertInventory(LOT, "ALLOCATED");
        insertTransaction(allocated, "ALLOCATE", 300L);
        insertTransaction(allocated, "ALLOCATE", 100L);
        insertTransaction(allocated, "ALLOCATE", 200L);

        List<Long> ids = repository.findAllocatedReferenceIdsByLot(LOT);

        assertThat(ids).containsExactly(100L, 200L, 300L);
    }
}
