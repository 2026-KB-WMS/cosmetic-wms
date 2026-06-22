package com.kb.cosmetic_wms.domain.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequestDto(
        @NotNull(message = "가맹점 ID는 필수입니다.")
        Long storeId,

        @NotNull(message = "창고 ID는 필수입니다.")
        Long warehouseId,

        List<OrderItemRequestDto> items
) {

    public record OrderItemRequestDto(
            Long productId,
            int quantity
    ) {
    }
}