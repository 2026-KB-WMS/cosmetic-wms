package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface InventoryJpaRepository extends JpaRepository<InventoryEntity, Long> {

    @Query(value = """
            SELECT i.inventory_id, i.available_quantity
            FROM inventory i
            WHERE i.product_id = :productId
              AND i.warehouse_id = :warehouseId
              AND i.alloc_status  = 'UNALLOCATED'
              AND i.quality_status = 'NORMAL'
              AND i.loc_status    = 'STORED'
              AND i.available_quantity > 0
            ORDER BY i.expiry_date ASC
            """, nativeQuery = true)
    List<Object[]> findAvailableForFefo(@Param("productId") Long productId,
                                        @Param("warehouseId") Long warehouseId);

    List<InventoryEntity> findByLotId(Long lotId);

    List<InventoryEntity> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryEntity i WHERE i.id = :id")
    Optional<InventoryEntity> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i FROM InventoryEntity i
            WHERE i.productId = :productId
              AND i.lotId = :lotId
              AND i.sectionId = :sectionId
              AND i.allocStatus = :allocStatus
              AND i.qualityStatus = :qualityStatus
              AND i.locStatus = :locStatus
              AND i.id <> :excludeId
            ORDER BY i.id ASC
            """)
    Optional<InventoryEntity> findMergeTargetForUpdate(
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("sectionId") Long sectionId,
            @Param("allocStatus") AllocStatus allocStatus,
            @Param("qualityStatus") QualityStatus qualityStatus,
            @Param("locStatus") LocStatus locStatus,
            @Param("excludeId") Long excludeId
    );
}