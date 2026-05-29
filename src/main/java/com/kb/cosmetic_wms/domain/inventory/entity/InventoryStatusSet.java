package com.kb.cosmetic_wms.domain.inventory.entity;

import com.kb.cosmetic_wms.domain.inventory.constants.InventoryConstants;
import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public record InventoryStatusSet(
        @Enumerated(EnumType.STRING)
        AllocStatus allocStatus,

        @Enumerated(EnumType.STRING)
        QualityStatus qualityStatus,

        @Enumerated(EnumType.STRING)
        LocStatus locStatus
) {
    public static InventoryStatusSet of(
            AllocStatus allocStatus, QualityStatus qualityStatus, LocStatus locStatus
    ) {
        validateStatusCombination(allocStatus, qualityStatus, locStatus);
        return new InventoryStatusSet(allocStatus, qualityStatus, locStatus);
    }

    private static void validateStatusCombination(
            AllocStatus allocStatus, QualityStatus qualityStatus, LocStatus locStatus
    ) {
        if (allocStatus == null || qualityStatus == null || locStatus == null) {
            throw new IllegalArgumentException(InventoryConstants.STATUS_SET_REQUIRED_MESSAGE);
        }

        if (allocStatus != AllocStatus.UNALLOCATED && !qualityStatus.isNormal()) {
            throw new IllegalArgumentException(InventoryConstants.INVALID_STATUS_SET_QUALITY_MESSAGE);
        }

        if (locStatus == LocStatus.MOVING && allocStatus != AllocStatus.UNALLOCATED) {
            throw new IllegalArgumentException(InventoryConstants.INVALID_STATUS_SET_ALLOC_MOVING_MESSAGE);
        }

        if (locStatus == LocStatus.MOVING && !qualityStatus.isNormal()) {
            throw new IllegalArgumentException(
                    String.format(InventoryConstants.INVALID_STATUS_SET_QUALITY_MOVING_MESSAGE, qualityStatus.getDescription())
            );
        }
    }
}
