package com.kb.cosmetic_wms.order.adapter.in.web;

import com.kb.cosmetic_wms.order.application.port.in.OrderResult;
import com.kb.cosmetic_wms.order.domain.enums.OrderStatus;

import java.util.List;

public record OrderResponse(
        Long id,
        Long storeId,
        Long warehouseId,
        OrderStatus orderStatus,
        List<OrderItemResponse> items
) {
    public record OrderItemResponse(Long id, Long productId, int quantity) {}

    public static OrderResponse from(OrderResult result) {
        return new OrderResponse(
                result.id(),
                result.storeId(),
                result.warehouseId(),
                result.orderStatus(),
                result.items().stream()
                        .map(item -> new OrderItemResponse(item.id(), item.productId(), item.quantity()))
                        .toList()
        );
    }
}