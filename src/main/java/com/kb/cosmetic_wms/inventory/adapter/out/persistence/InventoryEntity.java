package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.model.Inventory;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryStatusSet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDate;

@Entity
@Table(
        name = "inventory",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventory_unit",
                columnNames = {"product_id", "lot_id", "section_id", "alloc_status", "quality_status", "loc_status"}
        )
)
@Check(name = "chk_inventory_quantity", constraints = "quantity >= 0")
@Check(name = "chk_inventory_available_quantity", constraints = "available_quantity >= 0 AND available_quantity <= quantity")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class InventoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "alloc_status", length = 20, nullable = false)
    private AllocStatus allocStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality_status", length = 20, nullable = false)
    private QualityStatus qualityStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "loc_status", length = 20, nullable = false)
    private LocStatus locStatus;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    private InventoryEntity(Long id, Long productId, Long lotId, Long sectionId, Long warehouseId,
                            int quantity, int availableQuantity,
                            AllocStatus allocStatus, QualityStatus qualityStatus, LocStatus locStatus,
                            LocalDate expiryDate) {
        this.id = id;
        this.productId = productId;
        this.lotId = lotId;
        this.sectionId = sectionId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.allocStatus = allocStatus;
        this.qualityStatus = qualityStatus;
        this.locStatus = locStatus;
        this.expiryDate = expiryDate;
    }

    static InventoryEntity fromDomain(Inventory inventory) {
        return new InventoryEntity(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getLotId(),
                inventory.getSectionId(),
                inventory.getWarehouseId(),
                inventory.getQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getStatusSet().allocStatus(),
                inventory.getStatusSet().qualityStatus(),
                inventory.getStatusSet().locStatus(),
                inventory.getExpiryDate()
        );
    }

    Inventory toDomain() {
        return Inventory.reconstitute(
                id, productId, lotId, sectionId, warehouseId, quantity, availableQuantity,
                InventoryStatusSet.of(allocStatus, qualityStatus, locStatus),
                expiryDate
        );
    }
}
