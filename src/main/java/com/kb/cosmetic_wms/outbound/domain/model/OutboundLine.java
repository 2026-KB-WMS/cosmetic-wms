package com.kb.cosmetic_wms.outbound.domain.model;

public record OutboundLine(
        Long orderItemId,
        Long inventoryId,
        int targetQuantity
) {
}
