package com.kb.cosmetic_wms.outbound.application.port.in;

import java.util.List;

public record CreateOutboundFromAssignmentCommand(
        Long orderId,
        Long warehouseId,
        Long memberId,
        List<ItemDemand> items
) {
    public record ItemDemand(
            Long orderItemId,
            Long productId,
            int quantity
    ) {
    }
}
