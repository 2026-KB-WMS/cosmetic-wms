package com.kb.cosmetic_wms.inbound.application.event;

import java.time.LocalDate;
import java.util.List;

public record InboundCompletedEvent(
        Long inboundId,
        Long warehouseId,
        Long partnerId,
        List<LineSnapshot> lines
) {
    public record LineSnapshot(
            Long lineId,
            Long productId,
            int orderedQuantity,
            int receivedQuantity,
            LocalDate manufactureDate,
            LocalDate expirationDate
    ) {}
}
