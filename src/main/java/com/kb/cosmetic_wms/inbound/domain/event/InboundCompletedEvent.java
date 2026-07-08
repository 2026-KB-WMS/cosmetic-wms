package com.kb.cosmetic_wms.inbound.domain.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InboundCompletedEvent(
        Long inboundId,
        LocalDate inboundDate,
        Long warehouseId,
        Long partnerId,
        List<LineSnapshot> lines
) {
    public record LineSnapshot(
            Long lineId,
            Long productId,
            int orderedQuantity,
            int receivedQuantity,
            String manufacturerLotNumber,
            LocalDateTime manufacturingDate,
            LocalDateTime expirationDate
    ) {}
}
