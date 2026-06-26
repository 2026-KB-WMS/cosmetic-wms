package com.kb.cosmetic_wms.inbound.adapter.in.web;

import com.kb.cosmetic_wms.inbound.application.port.in.InboundResult;
import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;

import java.time.LocalDateTime;
import java.util.List;

public record InboundDetailResponse(
        Long id,
        Long warehouseId,
        Long partnerId,
        InboundStatus inboundStatus,
        LocalDateTime inboundDate,
        List<InboundItemResponse> items
) {
    public static InboundDetailResponse from(InboundResult result) {
        return new InboundDetailResponse(
                result.id(), result.warehouseId(), result.partnerId(),
                result.inboundStatus(), result.inboundDate(),
                result.items().stream().map(InboundItemResponse::from).toList()
        );
    }
}
