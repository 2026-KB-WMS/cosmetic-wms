package com.kb.cosmetic_wms.domain.outbound.fixture;

import com.kb.cosmetic_wms.domain.outbound.OutboundLine;
import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;
import com.kb.cosmetic_wms.domain.outbound.entity.OutboundItem;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundType;

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
        Outbound outbound = Outbound.create(ordersId, warehouseId, outboundType);
        outbound.addItem(new OutboundLine(1L, 1L, 10));
        return outbound;
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
        outbound.getOutboundItems().forEach(item -> item.changePickedQuantity(item.getTargetQuantity()));
        return outbound;
    }

    public Outbound buildShipped() {
        Outbound outbound = buildFullyPickedProcessing();
        outbound.ship();
        return outbound;
    }

    public Outbound buildCanceled() {
        Outbound outbound = build();
        outbound.cancel();
        return outbound;
    }
}
