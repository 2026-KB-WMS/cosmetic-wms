package com.kb.cosmetic_wms.order.adapter.in.web;

import com.kb.cosmetic_wms.order.application.port.in.CreateOrderCommand;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "가맹점 ID는 필수입니다.")
        Long storeId,

        List<OrderItemRequest> items
) {
    public record OrderItemRequest(Long productId, int quantity) {}

    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(storeId,
                items.stream()
                        .map(item -> new CreateOrderCommand.OrderLineCommand(item.productId(), item.quantity()))
                        .toList());
    }
}
