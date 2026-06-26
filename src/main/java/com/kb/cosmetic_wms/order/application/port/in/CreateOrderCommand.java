package com.kb.cosmetic_wms.order.application.port.in;

import java.util.List;

public record CreateOrderCommand(Long storeId, Long warehouseId, List<OrderLineCommand> lines) {
    public record OrderLineCommand(Long productId, int quantity) {}
}