package com.kb.cosmetic_wms.putaway.domain.event;

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