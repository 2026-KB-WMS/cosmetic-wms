package com.kb.cosmetic_wms.global.event;

import java.util.List;

public record InboundCompletedEvent(
        Long inboundId,
        Long warehouseId,
        List<ItemSnapshot> items
) {
    public record ItemSnapshot(
            Long inboundItemId,
            Long productId,
            Long lotId,
            Long sectionId,
            int quantity
    ) {}
}