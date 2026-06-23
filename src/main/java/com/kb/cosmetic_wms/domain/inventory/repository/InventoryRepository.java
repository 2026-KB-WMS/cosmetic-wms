package com.kb.cosmetic_wms.domain.inventory.repository;

import com.kb.cosmetic_wms.domain.inventory.entity.Inventory;
import com.kb.cosmetic_wms.domain.inventory.entity.InventoryStatusSet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query(value = """
            SELECT i.inventory_id, i.available_quantity
            FROM inventory i
            JOIN lot l ON i.lot_id = l.lot_id
            WHERE i.product_id = :productId
              AND i.warehouse_id = :warehouseId
              AND i.alloc_status  = 'UNALLOCATED'
              AND i.quality_status = 'NORMAL'
              AND i.loc_status    = 'STORED'
              AND i.available_quantity > 0
            ORDER BY l.expiration_date ASC
            """, nativeQuery = true)
    List<Object[]> findAvailableForFefo(@Param("productId") Long productId,
                                        @Param("warehouseId") Long warehouseId);

    List<Inventory> findByLotId(Long lotId);

    List<Inventory> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.id = :id")
    Optional<Inventory> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i FROM Inventory i
            WHERE i.productId = :productId
              AND i.lotId = :lotId
              AND i.sectionId = :sectionId
              AND i.statusSet.allocStatus   = :#{#statusSet.allocStatus()}
              AND i.statusSet.qualityStatus = :#{#statusSet.qualityStatus()}
              AND i.statusSet.locStatus     = :#{#statusSet.locStatus()}
              AND i.id <> :excludeId
            ORDER BY i.id ASC
            """)
    Optional<Inventory> findMergeTargetForUpdate(
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("sectionId") Long sectionId,
            @Param("statusSet") InventoryStatusSet statusSet,
            @Param("excludeId") Long excludeId
    );
}
