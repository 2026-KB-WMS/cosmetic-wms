package com.kb.cosmetic_wms.inbound.application.port.in;

import java.time.LocalDateTime;
import java.util.List;

public record ReceiveInboundCommand(List<LineItem> lines) {

    public record LineItem(
            Long lineId,
            int receivedQuantity,
            String manufacturerLotNumber,
            LocalDateTime manufacturingDate,
            LocalDateTime expirationDate
    ) {}
}
