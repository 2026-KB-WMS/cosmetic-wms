package com.kb.cosmetic_wms.domain.outbound.event;

import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;

public record OutboundAllocatedEvent(
        Long outboundId,
        Long ordersId
) {
    public static OutboundAllocatedEvent from(Outbound outbound) {
        return new OutboundAllocatedEvent(outbound.getId(), outbound.getOrdersId());
    }
}