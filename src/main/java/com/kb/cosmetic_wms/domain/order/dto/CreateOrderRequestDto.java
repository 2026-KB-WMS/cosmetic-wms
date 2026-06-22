package com.kb.cosmetic_wms.domain.order.dto;

import java.util.List;

public record CreateOrderRequestDto(
        Long storeId,
        Long warehouseId,
        List<OrderItemRequestDto> items
) {

    public record OrderItemRequestDto(
            Long productId,
            int quantity
    ) {
    }
}