package com.kb.cosmetic_wms.domain.outbound;

public record OutboundLine(
        Long orderItemId,
        Long inventoryId,
        int targetQuantity
) {
}
