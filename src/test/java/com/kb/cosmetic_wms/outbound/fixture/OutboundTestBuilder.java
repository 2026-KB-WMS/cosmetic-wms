package com.kb.cosmetic_wms.outbound.fixture;

import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundLine;

import java.time.LocalDateTime;
import java.util.List;

public class OutboundTestBuilder {

    private Long ordersId = 1L;
    private Long warehouseId = 10L;
    private OutboundType outboundType = OutboundType.ORDER;

    public OutboundTestBuilder ordersId(Long ordersId) {
        this.ordersId = ordersId;
        return this;
    }

    public OutboundTestBuilder warehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public Outbound build() {
        return Outbound.create(ordersId, warehouseId, outboundType, List.of(new OutboundLine(1L, 1L, 10)));
    }

    public Outbound buildAllocated() {
        Outbound outbound = build();
        outbound.allocate();
        return outbound;
    }

    public Outbound buildProcessing() {
        Outbound outbound = buildAllocated();
        outbound.startProcessing();
        return outbound;
    }

    public Outbound buildFullyPickedProcessing() {
        Outbound outbound = buildProcessing();
        outbound.getOutboundItems().forEach(item ->
                outbound.changeItemPickedQuantity(item.getInventoryId(), item.getTargetQuantity()));
        return outbound;
    }

    public Outbound buildShipped() {
        Outbound outbound = buildFullyPickedProcessing();
        outbound.ship(LocalDateTime.now());
        return outbound;
    }

    public Outbound buildCanceled() {
        Outbound outbound = build();
        outbound.cancel();
        return outbound;
    }
}