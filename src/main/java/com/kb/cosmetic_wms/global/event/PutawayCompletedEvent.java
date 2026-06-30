package com.kb.cosmetic_wms.global.event;

public record PutawayCompletedEvent(
        Long putawayOrderId,
        Long lotId,
        Long productId,
        Long warehouseId,
        Long sourceSectionId,
        Long targetSectionId,
        int quantity,
        boolean normalQuality,
        Long memberId
) {}
