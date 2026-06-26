package com.kb.cosmetic_wms.outbound.application.port.in;

import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;

import java.time.LocalDateTime;

public record OutboundResult(
        Long outboundId,
        Long ordersId,
        Long warehouseId,
        OutboundType outboundType,
        OutboundStatus outboundStatus,
        LocalDateTime outboundDate
) {
    public static OutboundResult from(Outbound outbound) {
        return new OutboundResult(
                outbound.getId(),
                outbound.getOrdersId(),
                outbound.getWarehouseId(),
                outbound.getOutboundType(),
                outbound.getOutboundStatus(),
                outbound.getOutboundDate()
        );
    }
}