package com.kb.cosmetic_wms.inventory.domain.model;

import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryErrorCode;
import com.kb.cosmetic_wms.inventory.domain.exception.InvalidInventoryStatusCombinationException;

public record InventoryStatusSet(
        AllocStatus allocStatus,
        QualityStatus qualityStatus,
        LocStatus locStatus
) {
    public static InventoryStatusSet of(
            AllocStatus allocStatus, QualityStatus qualityStatus, LocStatus locStatus
    ) {
        validate(allocStatus, qualityStatus, locStatus);
        return new InventoryStatusSet(allocStatus, qualityStatus, locStatus);
    }

    /**
     * 미할당·정상 품질이며 도킹 대기가 아닌 재고만 출고 가능 수량으로 잡을 수 있다.
     */
    public boolean isAvailableForOutbound() {
        return allocStatus == AllocStatus.UNALLOCATED
                && qualityStatus.isNormal()
                && locStatus != LocStatus.DOCKING;
    }

    public int availableQuantityFor(int quantity) {
        return isAvailableForOutbound() ? quantity : 0;
    }

    private static void validate(
            AllocStatus allocStatus, QualityStatus qualityStatus, LocStatus locStatus
    ) {
        if (allocStatus == null || qualityStatus == null || locStatus == null) {
            throw new InvalidInventoryStatusCombinationException(InventoryErrorCode.INVALID_STATUS_NULL);
        }

        if (allocStatus != AllocStatus.UNALLOCATED && !qualityStatus.isNormal()) {
            throw new InvalidInventoryStatusCombinationException(InventoryErrorCode.ALLOCATED_NON_NORMAL_QUALITY);
        }

        if (locStatus == LocStatus.MOVING && allocStatus != AllocStatus.UNALLOCATED) {
            throw new InvalidInventoryStatusCombinationException(InventoryErrorCode.MOVING_WITH_ALLOCATED);
        }

        if (locStatus == LocStatus.MOVING && !qualityStatus.isNormal()) {
            throw new InvalidInventoryStatusCombinationException(
                    InventoryErrorCode.MOVING_WITH_NON_NORMAL_QUALITY,
                    String.format("품질 상태가 %s인 결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다.", qualityStatus.getDescription())
            );
        }

        if (locStatus == LocStatus.DOCKING && allocStatus != AllocStatus.UNALLOCATED) {
            throw new InvalidInventoryStatusCombinationException(InventoryErrorCode.DOCKING_WITH_ALLOCATED);
        }
    }
}