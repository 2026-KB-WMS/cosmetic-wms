package com.kb.cosmetic_wms.inventory.domain.model;

import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Inventory {

    private Long id;
    private final Long productId;
    private final Long lotId;
    private final Long sectionId;
    private final Long warehouseId;
    private int quantity;
    private int availableQuantity;
    private InventoryStatusSet statusSet;
    private final LocalDate expiryDate;

    private Inventory(Long id, Long productId, Long lotId, Long sectionId, Long warehouseId,
                      int quantity, int availableQuantity, InventoryStatusSet statusSet, LocalDate expiryDate) {
        this.id = id;
        this.productId = productId;
        this.lotId = lotId;
        this.sectionId = sectionId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.statusSet = statusSet;
        this.expiryDate = expiryDate;
    }

    public static Inventory create(Long productId, Long lotId, Long sectionId, Long warehouseId,
                                   int quantity, int availableQuantity, InventoryStatusSet statusSet,
                                   LocalDate expiryDate) {
        validateQuantity(quantity);
        validateAvailableQuantity(quantity, availableQuantity);
        validateAvailableQuantityForQualityStatus(statusSet, availableQuantity);

        return new Inventory(null, productId, lotId, sectionId, warehouseId,
                quantity, availableQuantity, statusSet, expiryDate);
    }

    public static Inventory reconstitute(Long id, Long productId, Long lotId, Long sectionId, Long warehouseId,
                                         int quantity, int availableQuantity, InventoryStatusSet statusSet,
                                         LocalDate expiryDate) {
        return new Inventory(id, productId, lotId, sectionId, warehouseId,
                quantity, availableQuantity, statusSet, expiryDate);
    }

    public SplitResult allocate(int allocQuantity) {
        if (allocQuantity <= 0) {
            throw new IllegalArgumentException("할당할 수량은 0보다 커야 합니다.");
        }
        if (this.availableQuantity < allocQuantity) {
            throw new IllegalArgumentException("가용 재고가 부족하여 할당할 수 없습니다.");
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
                null, this.productId, this.lotId, this.sectionId, this.warehouseId,
                allocQuantity, 0, allocatedStatusSet, this.expiryDate
        ));
    }

    public SplitResult unallocate(int targetQuantity) {
        if (this.statusSet.allocStatus() != AllocStatus.ALLOCATED) {
            throw new IllegalStateException("할당된 재고만 할당 취소할 수 있습니다.");
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, this.statusSet.qualityStatus(), this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult startMoving(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException("이미 할당된 재고는 이동(MOVING) 시킬 수 없습니다. 할당 취소부터 진행해주세요.");
        }

        if (this.statusSet.locStatus() == LocStatus.MOVING) {
            throw new IllegalStateException("이미 이동 중인 재고입니다.");
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), LocStatus.MOVING
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult finishMoving(int targetQuantity) {
        if (this.statusSet.locStatus() != LocStatus.MOVING) {
            throw new IllegalStateException("이동 중(MOVING) 상태의 재고만 이동 완료 처리가 가능합니다.");
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), LocStatus.STORED
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult startInspecting(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException("이미 할당된 재고는 검수(INSPECTING) 상태로 변경할 수 없습니다. 할당 취소부터 진행해주세요.");
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.INSPECTING, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult restoreToNormalQuality(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException("할당된 재고는 품질 상태를 변경할 수 없습니다.");
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.NORMAL, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult holdForQualityIssue(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException("이미 할당된 재고는 출고 금지(HOLD) 처리할 수 없습니다. 할당 취소부터 진행해주세요.");
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.HOLD, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    public SplitResult scheduleForDiscard(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException("할당된 재고는 폐기 처리할 수 없습니다. 할당 취소부터 진행해주세요.");
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.DISCARD_SCHEDULED, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    private SplitResult splitAndChangeStatus(int targetQuantity, InventoryStatusSet nextStatusSet) {
        if (targetQuantity <= 0) {
            throw new IllegalArgumentException("재고 수량은 음수일 수 없습니다.");
        }
        if (this.quantity < targetQuantity) {
            throw new IllegalArgumentException("변경 요청 수량이 현재 보유한 재고 수량을 초과할 수 없습니다.");
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
                null, this.productId, this.lotId, this.sectionId, this.warehouseId,
                targetQuantity, nextAvailableQuantity, nextStatusSet, this.expiryDate
        ));
    }

    public void mergeFrom(Inventory other) {
        if (!this.productId.equals(other.productId)
                || !this.lotId.equals(other.lotId)
                || !this.sectionId.equals(other.sectionId)) {
            throw new IllegalArgumentException("상품·로트·섹션이 동일한 재고만 병합 가능합니다.");
        }
        if (!this.statusSet.equals(other.statusSet)) {
            throw new IllegalArgumentException("동일 상태의 재고만 병합 가능합니다.");
        }
        this.quantity += other.quantity;
        this.availableQuantity += other.availableQuantity;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("재고 수량은 음수일 수 없습니다.");
        }
    }

    private static void validateAvailableQuantity(int quantity, int availableQuantity) {
        if (quantity < availableQuantity) {
            throw new IllegalArgumentException("출고 가능 수량은 총 재고 수량을 초과할 수 없습니다.");
        }
    }

    private static void validateAvailableQuantityForQualityStatus(InventoryStatusSet statusSet, int availableQuantity) {
        if (!statusSet.qualityStatus().isNormal() && availableQuantity > 0) {
            throw new IllegalArgumentException(
                    String.format("품질 상태가 %s(%s)일 경우 출고 가능 수량은 0이어야 합니다.",
                            statusSet.qualityStatus().name(), statusSet.qualityStatus().getDescription()));
        }
    }
}