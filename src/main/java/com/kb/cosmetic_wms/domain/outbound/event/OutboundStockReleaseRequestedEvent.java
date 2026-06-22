package com.kb.cosmetic_wms.domain.outbound.event;

import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;

public record OutboundStockReleaseRequestedEvent(
        Long outboundId,
        Long ordersId
) {
    public static OutboundStockReleaseRequestedEvent from(Outbound outbound) {
        return new OutboundStockReleaseRequestedEvent(outbound.getId(), outbound.getOrdersId());
    }
}