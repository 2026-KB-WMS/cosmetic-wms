package com.kb.cosmetic_wms.order.adapter.in.web;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "가맹점 ID는 필수입니다.")
        Long storeId,

        @NotNull(message = "창고 ID는 필수입니다.")
        Long warehouseId,

        List<OrderItemRequest> items
) {
    public record OrderItemRequest(Long productId, int quantity) {}
}