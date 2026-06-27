package com.kb.cosmetic_wms.inbound.adapter.in.web;

import com.kb.cosmetic_wms.inbound.application.port.in.InboundLineResult;

import java.time.LocalDate;

public record InboundLineResponse(
        Long id,
        Long productId,
        int orderedQuantity,
        int receivedQuantity,
        LocalDate manufactureDate,
        LocalDate expirationDate
) {
    public static InboundLineResponse from(InboundLineResult result) {
        return new InboundLineResponse(
                result.id(), result.productId(),
                result.orderedQuantity(), result.receivedQuantity(),
                result.manufactureDate(), result.expirationDate()
        );
    }
}