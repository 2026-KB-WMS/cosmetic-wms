package com.kb.cosmetic_wms.domain.outbound.dto;

import com.kb.cosmetic_wms.domain.outbound.enums.OutboundType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOutboundRequestDto(
        @NotNull(message = "발주 ID는 필수입니다.")
        Long ordersId,

        @NotNull(message = "창고 ID는 필수입니다.")
        Long warehouseId,

        @NotNull(message = "출고 유형은 필수입니다.")
        OutboundType outboundType,

        List<OutboundItemRequestDto> items
) {
    public record OutboundItemRequestDto(
            Long orderItemId,
            Long inventoryId,
            int targetQuantity
    ) {
    }
}