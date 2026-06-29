package com.kb.cosmetic_wms.inbound.adapter.in.web;

import com.kb.cosmetic_wms.inbound.application.port.in.InboundLineResult;

import java.time.LocalDateTime;

public record InboundLineResponse(
        Long id,
        Long productId,
        int orderedQuantity,
        int receivedQuantity,
        String manufacturerLotNumber,
        LocalDateTime manufacturingDate,
        LocalDateTime expirationDate
) {
    public static InboundLineResponse from(InboundLineResult result) {
        return new InboundLineResponse(
                result.id(), result.productId(),
                result.orderedQuantity(), result.receivedQuantity(),
                result.manufacturerLotNumber(),
                result.manufacturingDate(), result.expirationDate()
        );
    }
}
