package com.kb.cosmetic_wms.order.application.port.in;

import java.util.List;

public record CreateOrderCommand(Long storeId, List<OrderLineCommand> lines) {
    public record OrderLineCommand(Long productId, int quantity) {}
}
