package com.kb.cosmetic_wms.inbound.fixture;

import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
import com.kb.cosmetic_wms.inbound.domain.model.InboundItem;
import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InboundTestBuilder {

    private Long id = null;
    private Long warehouseId = 1L;
    private Long partnerId = 1L;
    private LocalDateTime inboundDate = LocalDateTime.now().plusDays(1);
    private InboundStatus status = InboundStatus.SCHEDULED;
    private List<InboundItem> items = List.of();

    public InboundTestBuilder id(Long id) {
        this.id = id;
        return this;
    }

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

    public InboundTestBuilder status(InboundStatus status) {
        this.status = status;
        return this;
    }

    public InboundTestBuilder items(List<InboundItem> items) {
        this.items = items;
        return this;
    }

    public Inbound build() {
        if (id == null) {
            return Inbound.create(inboundDate, warehouseId, partnerId);
        }
        return Inbound.reconstitute(id, status, inboundDate, warehouseId, partnerId, items);
    }

    public Inbound buildInProgress() {
        if (id == null) {
            Inbound inbound = Inbound.create(inboundDate, warehouseId, partnerId);
            inbound.addItem(new InboundLine(1L, 100, LocalDate.now().minusDays(10), LocalDate.now().plusYears(2)));
            inbound.startExecution();
            return inbound;
        }
        InboundItem item = new InboundItemTestBuilder().id(1L).build();
        return Inbound.reconstitute(id, InboundStatus.IN_PROGRESS, inboundDate, warehouseId, partnerId, List.of(item));
    }
}
