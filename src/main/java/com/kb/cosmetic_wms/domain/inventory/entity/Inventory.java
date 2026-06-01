package com.kb.cosmetic_wms.domain.inventory.entity;

import com.kb.cosmetic_wms.domain.inventory.constants.InventoryConstants;
import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.storage.entity.Section;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    private int quantity;

    private int availableQuantity;

    @Embedded
    private InventoryStatusSet statusSet;

    private Inventory(Product product, Lot lot, Section section, Warehouse warehouse,
                      int quantity, int availableQuantity, InventoryStatusSet statusSet) {
        this.product = product;
        this.lot = lot;
        this.section = section;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.statusSet = statusSet;
    }

    public static Inventory create(Product product, Lot lot, Section section, Warehouse warehouse,
                                   int quantity, int availableQuantity, InventoryStatusSet statusSet) {

        validateQuantity(quantity);
        validateAvailableQuantity(quantity, availableQuantity);
        validateAvailableQuantityForQualityStatus(statusSet, availableQuantity);
        return new Inventory(product, lot, section, warehouse, quantity, availableQuantity, statusSet);
    }

    // 출고 할당 메서드 (UNALLOCATED -> ALLOCATED)
    public Inventory allocate(int allocQuantity) {
        if (allocQuantity <= 0) {
            throw new IllegalArgumentException(InventoryConstants.INVALID_ALLOCATE_QUANTITY_MESSAGE);
        }
        if (this.availableQuantity < allocQuantity) {
            throw new IllegalArgumentException(InventoryConstants.LACK_OF_AVAILABLE_QUANTITY_MESSAGE);
        }

        // 요청 수량이 총 수량과 일치하는 경우
        if (this.quantity == allocQuantity) {
            this.availableQuantity = 0;
            this.statusSet = InventoryStatusSet.of(
                    AllocStatus.ALLOCATED, this.statusSet.qualityStatus(), this.statusSet.locStatus()
            );
            return this;
        }

        this.quantity -= allocQuantity;
        this.availableQuantity -= allocQuantity;

        InventoryStatusSet allocatedStatusSet = InventoryStatusSet.of(
                AllocStatus.ALLOCATED,
                this.statusSet.qualityStatus(),
                this.statusSet.locStatus()
        );

        return new Inventory(
                this.product,
                this.lot,
                this.section,
                this.warehouse,
                allocQuantity,
                0,
                allocatedStatusSet
        );
    }

    // 품질 상태 변경 (불량 발견, 검수 완료 등)
    public InventoryStatusSet changeQualityStatus(QualityStatus nextQualityStatus) {
        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), nextQualityStatus, this.statusSet.locStatus()
        );

        InventoryStatusSet prevStatusSet = this.statusSet;
        this.statusSet = nextStatusSet;

        if (!this.statusSet.qualityStatus().isNormal()) {
            this.availableQuantity = 0;
        }

        return prevStatusSet;
    }

    // 위치/이동 상태 변경
    public InventoryStatusSet changeLocStatus(LocStatus nextLocStatus) {
        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), nextLocStatus
        );

        InventoryStatusSet prevStatusSet = this.statusSet;
        this.statusSet = nextStatusSet;

        return prevStatusSet;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(InventoryConstants.INVALID_QUANTITY_MESSAGE);
        }
    }

    private static void validateAvailableQuantity(int quantity, int availableQuantity) {
        if (quantity < availableQuantity) {
            throw new IllegalArgumentException(InventoryConstants.OVER_AVAILABLE_QUANTITY_MESSAGE);
        }
    }

    private static void validateAvailableQuantityForQualityStatus(InventoryStatusSet statusSet, int availableQuantity) {
        if (!statusSet.qualityStatus().isNormal() && availableQuantity > 0) {
            throw new IllegalArgumentException(
                    String.format(InventoryConstants.INVALID_QUALITY_AVAILABLE_QUANTITY_MESSAGE,
                            statusSet.qualityStatus().name(), statusSet.qualityStatus().getDescription()));
        }
    }
}
