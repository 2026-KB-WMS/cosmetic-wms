package com.kb.cosmetic_wms.outbound.adapter.in.web;

import com.kb.cosmetic_wms.outbound.application.port.in.CreateOutboundCommand;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundLine;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOutboundRequest(
        @NotNull(message = "발주 ID는 필수입니다.")
        Long ordersId,

        @NotNull(message = "창고 ID는 필수입니다.")
        Long warehouseId,

        @NotNull(message = "출고 유형은 필수입니다.")
        OutboundType outboundType,

        List<OutboundItemRequest> items
) {
    public record OutboundItemRequest(
            Long orderItemId,
            Long inventoryId,
            int targetQuantity
    ) {
    }

    public CreateOutboundCommand toCommand() {
        List<OutboundLine> lines = items.stream()
                .map(item -> new OutboundLine(item.orderItemId(), item.inventoryId(), item.targetQuantity()))
                .toList();
        return new CreateOutboundCommand(ordersId, warehouseId, outboundType, lines);
    }
}