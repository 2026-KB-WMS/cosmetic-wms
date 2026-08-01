package com.kb.ordering.assignment.application.port.in;

import java.util.List;

public record AssignWarehouseCommand(
        Long orderId,
        Long storeId,
        List<ItemDemand> items
) {
    public record ItemDemand(Long orderItemId, Long productId, int quantity) {}
}
