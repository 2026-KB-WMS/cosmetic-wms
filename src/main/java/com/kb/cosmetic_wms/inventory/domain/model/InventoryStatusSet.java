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
    }
}