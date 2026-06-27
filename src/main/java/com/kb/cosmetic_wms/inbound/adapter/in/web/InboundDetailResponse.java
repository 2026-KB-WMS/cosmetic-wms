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
        List<InboundLineResponse> lines
) {
    public static InboundDetailResponse from(InboundResult result) {
        return new InboundDetailResponse(
                result.id(), result.warehouseId(), result.partnerId(),
                result.inboundStatus(), result.inboundDate(),
                result.lines().stream().map(InboundLineResponse::from).toList()
        );
    }
}
