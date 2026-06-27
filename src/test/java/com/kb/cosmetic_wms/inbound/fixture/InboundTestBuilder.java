package com.kb.cosmetic_wms.inbound.fixture;

import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
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
    private List<InboundLine> lines = List.of();

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

    public InboundTestBuilder lines(List<InboundLine> lines) {
        this.lines = lines;
        return this;
    }

    public Inbound build() {
        if (id == null) {
            List<InboundLine> defaultLines = lines.isEmpty() ? defaultLines() : lines;
            return Inbound.create(inboundDate, warehouseId, partnerId, defaultLines);
        }
        return Inbound.reconstitute(id, status, inboundDate, warehouseId, partnerId, lines);
    }

    public Inbound buildWithLines(List<InboundLine> lines) {
        if (id == null) {
            return Inbound.create(inboundDate, warehouseId, partnerId, lines);
        }
        return Inbound.reconstitute(id, status, inboundDate, warehouseId, partnerId, lines);
    }

    public Inbound buildReceived() {
        InboundLine line = InboundLine.reconstitute(1L, 1L, 100, 100,
                LocalDate.now().minusDays(10), LocalDate.now().plusYears(2));
        return Inbound.reconstitute(id != null ? id : 1L, InboundStatus.RECEIVED,
                inboundDate, warehouseId, partnerId, List.of(line));
    }

    private static List<InboundLine> defaultLines() {
        return List.of(InboundLine.create(1L, 100,
                LocalDate.now().minusDays(10), LocalDate.now().plusYears(2)));
    }
}