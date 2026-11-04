package com.kb.cosmetic_wms.outbound.application.port.in;

import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundItem;

import java.time.LocalDateTime;
import java.util.List;

public record OutboundDetailResult(
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
        static Item from(OutboundItem item) {
            return new Item(item.getId(), item.getOrderItemId(), item.getInventoryId(),
                    item.getTargetQuantity(), item.getPickedQuantity());
        }
    }

    public static OutboundDetailResult from(Outbound outbound) {
        return new OutboundDetailResult(
                outbound.getId(),
                outbound.getOrdersId(),
                outbound.getWarehouseId(),
                outbound.getOutboundType(),
                outbound.getOutboundStatus(),
                outbound.getOutboundDate(),
                outbound.getOutboundItems().stream().map(Item::from).toList()
        );
    }
}
