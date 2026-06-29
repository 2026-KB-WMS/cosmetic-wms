package com.kb.cosmetic_wms.inbound.application.port.in;

import java.time.LocalDateTime;
import java.util.List;

public record RegisterInboundCommand(
        Long warehouseId,
        Long partnerId,
        LocalDateTime inboundDate,
        List<LineItem> lines
) {
    public record LineItem(
            Long productId,
            int orderedQuantity
    ) {}
}
