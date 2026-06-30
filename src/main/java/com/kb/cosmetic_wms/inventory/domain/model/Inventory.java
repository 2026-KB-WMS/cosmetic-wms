package com.kb.cosmetic_wms.inventory.domain.model;

import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryErrorCode;
import com.kb.cosmetic_wms.inventory.domain.exception.InsufficientInventoryException;
import com.kb.cosmetic_wms.inventory.domain.exception.InvalidInventoryQuantityException;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryMergeException;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryStateTransitionException;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Inventory {

    private Long id;
    private final Long productId;
    private final Long lotId;
    private Long sectionId;
    private final Long warehouseId;
    private InventoryQuantity quantities;
    private InventoryStatusSet statusSet;
    private final LocalDate expiryDate;

    private Inventory(Long id, Long productId, Long lotId, Long sectionId, Long warehouseId,
                      InventoryQuantity quantities, InventoryStatusSet statusSet, LocalDate expiryDate) {
        this.id = id;
        this.productId = productId;
        this.lotId = lotId;
        this.sectionId = sectionId;
        this.warehouseId = warehouseId;
        this.quantities = quantities;
        this.statusSet = statusSet;
        this.expiryDate = expiryDate;
    }

    public static Inventory create(Long productId, Long lotId, Long sectionId, Long warehouseId,
                                   int quantity, int availableQuantity, InventoryStatusSet statusSet,
                                   LocalDate expiryDate) {
        InventoryQuantity quantities = InventoryQuantity.of(quantity, availableQuantity);
        validateAvailableQuantityForQualityStatus(statusSet, availableQuantity);

        return new Inventory(null, productId, lotId, sectionId, warehouseId,
                quantities, statusSet, expiryDate);
    }

    public static Inventory reconstitute(Long id, Long productId, Long lotId, Long sectionId, Long warehouseId,
                                         int quantity, int availableQuantity, InventoryStatusSet statusSet,
                                         LocalDate expiryDate) {
        return new Inventory(id, productId, lotId, sectionId, warehouseId,
                InventoryQuantity.fromPersistence(quantity, availableQuantity), statusSet, expiryDate);
    }

    public int getQuantity() {
        return quantities.total();
    }

    public int getAvailableQuantity() {
        return quantities.available();
    }

    public SplitResult allocate(int allocQuantity) {
        if (allocQuantity <= 0) {
            throw new InvalidInventoryQuantityException(InventoryErrorCode.INVALID_ALLOC_QUANTITY);
        }
        if (this.quantities.available() < allocQuantity) {
            throw new InsufficientInventoryException();
        }

        InventoryStatusSet allocatedStatusSet = InventoryStatusSet.of(
                AllocStatus.ALLOCATED, this.statusSet.qualityStatus(), this.statusSet.locStatus()
        );

        if (this.quantities.total() == allocQuantity) {
            this.quantities = InventoryQuantity.of(this.quantities.total(), 0);
            this.statusSet = allocatedStatusSet;
            return SplitResult.noSplit(this);
        }

        this.quantities = InventoryQuantity.of(
                this.quantities.total() - allocQuantity,
                this.quantities.available() - allocQuantity
        );

        return SplitResult.split(this, new Inventory(
                null, this.productId, this.lotId, this.sectionId, this.warehouseId,
                InventoryQuantity.of(allocQuantity, 0), allocatedStatusSet, this.expiryDate
        ));
    }

    public SplitResult unallocate(int targetQuantity) {
        if (this.statusSet.allocStatus() != AllocStatus.ALLOCATED) {
            throw new InventoryStateTransitionException(InventoryErrorCode.NOT_ALLOCATED);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, this.statusSet.qualityStatus(), this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult startMoving(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new InventoryStateTransitionException(InventoryErrorCode.ALLOCATED_CANNOT_MOVE);
        }

        if (this.statusSet.locStatus() == LocStatus.MOVING) {
            throw new InventoryStateTransitionException(InventoryErrorCode.ALREADY_MOVING);
        }

        if (this.statusSet.locStatus() == LocStatus.DOCKING) {
            throw new InventoryStateTransitionException(InventoryErrorCode.DOCKING_CANNOT_MOVE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), LocStatus.MOVING
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult finishMoving(int targetQuantity) {
        if (this.statusSet.locStatus() != LocStatus.MOVING) {
            throw new InventoryStateTransitionException(InventoryErrorCode.NOT_MOVING);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), LocStatus.STORED
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult startInspecting(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new InventoryStateTransitionException(InventoryErrorCode.ALLOCATED_CANNOT_INSPECT);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.INSPECTING, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult restoreToNormalQuality(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new InventoryStateTransitionException(InventoryErrorCode.ALLOCATED_CANNOT_CHANGE_QUALITY);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.NORMAL, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult holdForQualityIssue(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new InventoryStateTransitionException(InventoryErrorCode.ALLOCATED_CANNOT_HOLD);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.HOLD, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult scheduleForDiscard(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new InventoryStateTransitionException(InventoryErrorCode.ALLOCATED_CANNOT_DISCARD);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.DISCARD_SCHEDULED, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    private SplitResult splitAndChangeStatus(int targetQuantity, InventoryStatusSet nextStatusSet) {
        if (targetQuantity <= 0) {
            throw new InvalidInventoryQuantityException(InventoryErrorCode.INVALID_QUANTITY);
        }
        if (this.quantities.total() < targetQuantity) {
            throw new InvalidInventoryQuantityException(InventoryErrorCode.QUANTITY_EXCEEDS_STOCK);
        }

        int nextAvailableQuantity = 0;
        if (nextStatusSet.qualityStatus().isNormal() && nextStatusSet.allocStatus() == AllocStatus.UNALLOCATED) {
            nextAvailableQuantity = targetQuantity;
        }

        if (this.quantities.total() == targetQuantity) {
            this.statusSet = nextStatusSet;
            this.quantities = InventoryQuantity.of(this.quantities.total(), nextAvailableQuantity);
            return SplitResult.noSplit(this);
        }

        int newTotal = this.quantities.total() - targetQuantity;
        int newAvailable = this.quantities.available();
        if (this.statusSet.qualityStatus().isNormal() && this.statusSet.allocStatus() == AllocStatus.UNALLOCATED) {
            newAvailable -= targetQuantity;
        }
        this.quantities = InventoryQuantity.of(newTotal, newAvailable);

        return SplitResult.split(this, new Inventory(
                null, this.productId, this.lotId, this.sectionId, this.warehouseId,
                InventoryQuantity.of(targetQuantity, nextAvailableQuantity), nextStatusSet, this.expiryDate
        ));
    }

    public void completePutaway(Long targetSectionId) {
        if (this.statusSet.locStatus() != LocStatus.DOCKING) {
            throw new InventoryStateTransitionException(InventoryErrorCode.NOT_DOCKING);
        }
        this.sectionId = targetSectionId;
        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, this.statusSet.qualityStatus(), LocStatus.STORED
        );
        int newAvailable = this.statusSet.qualityStatus().isNormal() ? this.quantities.total() : 0;
        this.statusSet = nextStatusSet;
        this.quantities = InventoryQuantity.of(this.quantities.total(), newAvailable);
    }

    public void mergeFrom(Inventory other) {
        if (!this.productId.equals(other.productId)
                || !this.lotId.equals(other.lotId)
                || !this.sectionId.equals(other.sectionId)) {
            throw new InventoryMergeException(InventoryErrorCode.INCOMPATIBLE_MERGE_KEY);
        }
        if (!this.statusSet.equals(other.statusSet)) {
            throw new InventoryMergeException(InventoryErrorCode.INCOMPATIBLE_MERGE_STATUS);
        }
        this.quantities = InventoryQuantity.of(
                this.quantities.total() + other.quantities.total(),
                this.quantities.available() + other.quantities.available()
        );
    }

    private static void validateAvailableQuantityForQualityStatus(InventoryStatusSet statusSet, int availableQuantity) {
        if (!statusSet.qualityStatus().isNormal() && availableQuantity > 0) {
            throw new InvalidInventoryQuantityException(
                    InventoryErrorCode.INVALID_AVAILABLE_FOR_QUALITY,
                    String.format("품질 상태가 %s(%s)일 경우 출고 가능 수량은 0이어야 합니다.",
                            statusSet.qualityStatus().name(), statusSet.qualityStatus().getDescription())
            );
        }
        if (statusSet.locStatus() == LocStatus.DOCKING && availableQuantity > 0) {
            throw new InvalidInventoryQuantityException(
                    InventoryErrorCode.INVALID_AVAILABLE_FOR_DOCKING
            );
        }
    }
}
