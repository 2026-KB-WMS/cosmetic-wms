package com.kb.cosmetic_wms.domain.inventory.repository;

import com.kb.cosmetic_wms.domain.inventory.entity.Inventory;
import com.kb.cosmetic_wms.domain.inventory.entity.InventoryStatusSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByLotId(Long lotId);

    List<Inventory> findByProductId(Long productId);

    @Query("""
            SELECT i FROM Inventory i
            WHERE i.productId = :productId
              AND i.lotId = :lotId
              AND i.sectionId = :sectionId
              AND i.statusSet.allocStatus   = :#{#statusSet.allocStatus()}
              AND i.statusSet.qualityStatus = :#{#statusSet.qualityStatus()}
              AND i.statusSet.locStatus     = :#{#statusSet.locStatus()}
              AND i.id <> :excludeId
            """)
    Optional<Inventory> findMergeTarget(
            @Param("productId") Long productId,
            @Param("lotId") Long lotId,
            @Param("sectionId") Long sectionId,
            @Param("statusSet") InventoryStatusSet statusSet,
            @Param("excludeId") Long excludeId
    );
}
