package com.kb.cosmetic_wms.inbound.application.port.in;

import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;

import java.time.LocalDate;

public record InboundLineResult(
        Long id,
        Long productId,
        int orderedQuantity,
        int receivedQuantity,
        LocalDate manufactureDate,
        LocalDate expirationDate
) {
    public static InboundLineResult from(InboundLine line) {
        return new InboundLineResult(
                line.getId(), line.getProductId(),
                line.getOrderedQuantity(), line.getReceivedQuantity(),
                line.getManufactureDate(), line.getExpirationDate()
        );
    }
}