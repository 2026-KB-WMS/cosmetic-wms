package com.kb.cosmetic_wms.outbound.domain.event;

import java.util.List;

public record OutboundAllocatedEvent(
        Long outboundId,
        Long ordersId,
        List<ItemSnapshot> items
) {
    public record ItemSnapshot(Long inventoryId, int targetQuantity) {}
}