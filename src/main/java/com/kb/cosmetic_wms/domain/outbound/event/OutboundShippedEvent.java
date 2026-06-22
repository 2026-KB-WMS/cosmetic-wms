package com.kb.cosmetic_wms.domain.outbound.event;

import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;

public record OutboundShippedEvent(
        Long outboundId,
        Long ordersId
) {
    public static OutboundShippedEvent from(Outbound outbound) {
        return new OutboundShippedEvent(outbound.getId(), outbound.getOrdersId());
    }
}