package com.kb.cosmetic_wms.outbound.adapter.in.web;

import com.kb.cosmetic_wms.outbound.application.port.in.OutboundDetailResult;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;

import java.time.LocalDateTime;
import java.util.List;

public record OutboundDetailResponse(
        Long outboundId,
        Long ordersId,
        Long warehouseId,
        OutboundType outboundType,
        OutboundStatus outboundStatus,
        LocalDateTime outboundDate,
        List<Item> items
) {
    public record Item(
            Long outboundItemId,
            Long orderItemId,
            Long inventoryId,
            int targetQuantity,
            int pickedQuantity
    ) {
    }

    public static OutboundDetailResponse from(OutboundDetailResult result) {
        return new OutboundDetailResponse(
                result.outboundId(),
                result.ordersId(),
                result.warehouseId(),
                result.outboundType(),
                result.outboundStatus(),
                result.outboundDate(),
                result.items().stream()
                        .map(i -> new Item(i.outboundItemId(), i.orderItemId(), i.inventoryId(),
                                i.targetQuantity(), i.pickedQuantity()))
                        .toList()
        );
    }
}
