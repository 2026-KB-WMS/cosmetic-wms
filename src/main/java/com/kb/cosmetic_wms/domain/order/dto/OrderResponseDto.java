package com.kb.cosmetic_wms.domain.order.dto;

import com.kb.cosmetic_wms.domain.order.entity.Orders;
import com.kb.cosmetic_wms.domain.order.enums.OrderStatus;

import java.util.List;

public record OrderResponseDto(
        Long id,
        Long storeId,
        Long warehouseId,
        OrderStatus orderStatus,
        List<OrderItemResponseDto> items
) {

    public record OrderItemResponseDto(
            Long id,
            Long productId,
            int quantity
    ) {
    }

    public static OrderResponseDto from(Orders order) {
        return new OrderResponseDto(
                order.getId(),
                order.getStoreId(),
                order.getWarehouseId(),
                order.getOrderStatus(),
                order.getOrderItems().stream()
                        .map(item -> new OrderItemResponseDto(item.getId(), item.getProductId(), item.getQuantity()))
                        .toList()
        );
    }
}