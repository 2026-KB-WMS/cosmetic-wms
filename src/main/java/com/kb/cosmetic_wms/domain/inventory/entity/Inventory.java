package com.kb.cosmetic_wms.domain.inventory.entity;

import com.kb.cosmetic_wms.domain.inventory.constants.InventoryConstants;
import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

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
@Getter
public class Inventory extends BaseEntity {

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

    @Embedded
    private InventoryStatusSet statusSet;

    private Inventory(Long productId, Long lotId, Long sectionId, Long warehouseId,
                      int quantity, int availableQuantity, InventoryStatusSet statusSet) {
        this.productId = productId;
        this.lotId = lotId;
        this.sectionId = sectionId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.statusSet = statusSet;
    }

    public static Inventory create(Long productId, Long lotId, Long sectionId, Long warehouseId,
                                   int quantity, int availableQuantity, InventoryStatusSet statusSet) {
        validateQuantity(quantity);
        validateAvailableQuantity(quantity, availableQuantity);
        validateAvailableQuantityForQualityStatus(statusSet, availableQuantity);

        return new Inventory(productId, lotId, sectionId, warehouseId,
                quantity, availableQuantity, statusSet);
    }

    /**
     * 출고 할당 처리 (UNALLOCATED -> ALLOCATED 재고 분할)
     */
    public SplitResult allocate(int allocQuantity) {
        if (allocQuantity <= 0) {
            throw new IllegalArgumentException(InventoryConstants.INVALID_ALLOCATE_QUANTITY_MESSAGE);
        }
        if (this.availableQuantity < allocQuantity) {
            throw new IllegalArgumentException(InventoryConstants.LACK_OF_AVAILABLE_QUANTITY_MESSAGE);
        }

        InventoryStatusSet allocatedStatusSet = InventoryStatusSet.of(
                AllocStatus.ALLOCATED, this.statusSet.qualityStatus(), this.statusSet.locStatus()
        );

        if (this.quantity == allocQuantity) {
            this.availableQuantity = 0;
            this.statusSet = allocatedStatusSet;
            return SplitResult.noSplit(this);
        }

        this.quantity -= allocQuantity;
        this.availableQuantity -= allocQuantity;

        return SplitResult.split(this, new Inventory(
                this.productId, this.lotId, this.sectionId, this.warehouseId,
                allocQuantity, 0, allocatedStatusSet
        ));
    }

    /**
     * 출고 할당 취소 (ALLOCATED -> UNALLOCATED 복귀)
     */
    public SplitResult unallocate(int targetQuantity) {
        if (this.statusSet.allocStatus() != AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.UNALLOCATE_FOR_ALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, this.statusSet.qualityStatus(), this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 창고 내 재고 이동 시작 (STORED -> MOVING)
     */
    public SplitResult startMoving(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.START_MOVING_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        if (this.statusSet.locStatus() == LocStatus.MOVING) {
            throw new IllegalStateException(InventoryConstants.ALREADY_MOVING_INVENTORY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), LocStatus.MOVING
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 창고 내 재고 이동 완료 (MOVING -> STORED)
     */
    public SplitResult finishMoving(int targetQuantity) {
        if (this.statusSet.locStatus() != LocStatus.MOVING) {
            throw new IllegalStateException(InventoryConstants.FINISH_MOVING_FOR_MOVING_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), LocStatus.STORED
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 품질 검수 시작 (정상 -> INSPECTING)
     */
    public SplitResult startInspecting(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.START_INSPECTING_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.INSPECTING, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 품질 검수 통과 / 보류 해제 (불량/검수중 -> NORMAL)
     */
    public SplitResult restoreToNormalQuality(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.CHANGE_QUALITY_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.NORMAL, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 품질 이슈로 인한 출고 금지 처리 (정상 -> HOLD)
     */
    public SplitResult holdForQualityIssue(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.HOLD_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.HOLD, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 폐기 예정 처리 (-> DISCARD_SCHEDULED)
     */
    public SplitResult scheduleForDiscard(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.DISCARD_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.DISCARD_SCHEDULED, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    private SplitResult splitAndChangeStatus(int targetQuantity, InventoryStatusSet nextStatusSet) {
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
            return SplitResult.noSplit(this);
        }

        this.quantity -= targetQuantity;

        if (this.statusSet.qualityStatus().isNormal() && this.statusSet.allocStatus() == AllocStatus.UNALLOCATED) {
            this.availableQuantity -= targetQuantity;
        }

        return SplitResult.split(this, new Inventory(
                this.productId, this.lotId, this.sectionId, this.warehouseId,
                targetQuantity, nextAvailableQuantity, nextStatusSet
        ));
    }

    public void mergeFrom(Inventory other) {
        if (!this.productId.equals(other.productId)
                || !this.lotId.equals(other.lotId)
                || !this.sectionId.equals(other.sectionId)) {
            throw new IllegalArgumentException(InventoryConstants.MERGE_KEY_MISMATCH_MESSAGE);
        }
        if (!this.statusSet.equals(other.statusSet)) {
            throw new IllegalArgumentException(InventoryConstants.MERGE_STATUS_MISMATCH_MESSAGE);
        }
        this.quantity += other.quantity;
        this.availableQuantity += other.availableQuantity;
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
