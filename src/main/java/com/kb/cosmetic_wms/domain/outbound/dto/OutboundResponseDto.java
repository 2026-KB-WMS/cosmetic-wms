package com.kb.cosmetic_wms.domain.outbound.dto;

import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundStatus;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundType;

import java.time.LocalDateTime;

public record OutboundResponseDto(
        Long outboundId,
        Long ordersId,
        Long warehouseId,
        OutboundType outboundType,
        OutboundStatus outboundStatus,
        LocalDateTime outboundDate
) {
    public static OutboundResponseDto from(Outbound outbound) {
        return new OutboundResponseDto(
                outbound.getId(),
                outbound.getOrdersId(),
                outbound.getWarehouseId(),
                outbound.getOutboundType(),
                outbound.getOutboundStatus(),
                outbound.getOutboundDate()
        );
    }
}