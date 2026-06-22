package com.kb.cosmetic_wms.domain.outbound.event;

import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;

public record OutboundCanceledEvent(
        Long outboundId,
        Long ordersId
) {
    public static OutboundCanceledEvent from(Outbound outbound) {
        return new OutboundCanceledEvent(outbound.getId(), outbound.getOrdersId());
    }
}