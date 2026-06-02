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

    // 할당 취소
    public Inventory unallocate(int targetQuantity) {
        if (this.statusSet.allocStatus() != AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.UNALLOCATE_FOR_ALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, this.statusSet.qualityStatus(), this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    // 이동 시작 (보관중 -> 이동중)
    public Inventory startMoving(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.START_MOVING_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), LocStatus.MOVING
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    // 이동 완료 (이동중 -> 보관중 복귀)
    public Inventory finishMoving(int targetQuantity) {
        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), LocStatus.STORED
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    // 반품/입고 직후 검수 시작 (정상 -> 검수중)
    public Inventory startInspecting(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.START_INSPECTING_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.INSPECTING, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    // 검수 통과 / 보류 해제 (불량/검수중 -> 정상)
    public Inventory restoreToNormalQuality(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.CHANGE_QUALITY_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.NORMAL, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    // 품질 이슈로 인한 출고 금지 (정상 -> HOLD)
    public Inventory holdForQualityIssue(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.HOLD_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.HOLD, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    // 심각한 파손으로 인한 폐기 예정 처리 (-> DISCARD_SCHEDULED)
    public Inventory scheduleForDiscard(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.DISCARD_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.DISCARD_SCHEDULED, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    private Inventory splitAndChangeStatus(int targetQuantity, InventoryStatusSet nextStatusSet) {
        if (targetQuantity <= 0) {
            throw new IllegalArgumentException(InventoryConstants.INVALID_QUANTITY_MESSAGE);
        }
        if (this.quantity < targetQuantity) {
            throw new IllegalArgumentException(InventoryConstants.EXCEED_INVENTORY_QUANTITY_MESSAGE);
        }

        int nextAvailableQuantity = 0;
        if (nextStatusSet.qualityStatus().isNormal() && nextStatusSet.allocStatus() == AllocStatus.UNALLOCATED) {
            nextAvailableQuantity = targetQuantity;
        }

        if (this.quantity == targetQuantity) {
            this.statusSet = nextStatusSet;
            this.availableQuantity = nextAvailableQuantity;
            return this;
        }

        this.quantity -= targetQuantity;

        if (this.statusSet.qualityStatus().isNormal() && this.statusSet.allocStatus() == AllocStatus.UNALLOCATED) {
            this.availableQuantity = Math.max(0, this.availableQuantity - targetQuantity);
        }

        return new Inventory(
                this.product, this.lot, this.section, this.warehouse,
                targetQuantity, nextAvailableQuantity, nextStatusSet
        );
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
