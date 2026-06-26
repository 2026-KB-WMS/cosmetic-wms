package com.kb.cosmetic_wms.outbound.adapter.in.web;

import com.kb.cosmetic_wms.outbound.application.port.in.OutboundResult;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;

import java.time.LocalDateTime;

public record OutboundResponse(
        Long outboundId,
        Long ordersId,
        Long warehouseId,
        OutboundType outboundType,
        OutboundStatus outboundStatus,
        LocalDateTime outboundDate
) {
    public static OutboundResponse from(OutboundResult result) {
        return new OutboundResponse(
                result.outboundId(),
                result.ordersId(),
                result.warehouseId(),
                result.outboundType(),
                result.outboundStatus(),
                result.outboundDate()
        );
    }
}