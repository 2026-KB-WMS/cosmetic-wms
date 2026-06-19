package com.kb.cosmetic_wms.domain.inbound.fixture;

import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;

import java.time.LocalDateTime;

public class InboundTestBuilder {

    private Long warehouseId = 1L;
    private Long partnerId = 1L;
    private LocalDateTime inboundDate = LocalDateTime.now().plusDays(1);

    public InboundTestBuilder warehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public InboundTestBuilder partnerId(Long partnerId) {
        this.partnerId = partnerId;
        return this;
    }

    public InboundTestBuilder inboundDate(LocalDateTime inboundDate) {
        this.inboundDate = inboundDate;
        return this;
    }

    public Inbound build() {
        return Inbound.create(inboundDate, warehouseId, partnerId);
    }

    public Inbound buildInProgress() {
        Inbound inbound = build();
        inbound.startExecution();
        return inbound;
    }
}
