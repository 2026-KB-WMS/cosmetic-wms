package com.kb.cosmetic_wms.inbound.application.port.in;

import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;

import java.time.LocalDateTime;

public record InboundLineResult(
        Long id,
        Long productId,
        int orderedQuantity,
        int receivedQuantity,
        String manufacturerLotNumber,
        LocalDateTime manufacturingDate,
        LocalDateTime expirationDate
) {
    public static InboundLineResult from(InboundLine line) {
        return new InboundLineResult(
                line.getId(), line.getProductId(),
                line.getOrderedQuantity(), line.getReceivedQuantity(),
                line.getManufacturerLotNumber(),
                line.getManufacturingDate(), line.getExpirationDate()
        );
    }
}
