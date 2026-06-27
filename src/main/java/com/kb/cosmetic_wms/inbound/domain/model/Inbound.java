package com.kb.cosmetic_wms.inbound.domain.model;

import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inbound.domain.exception.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class Inbound {

    private Long id;
    private InboundStatus inboundStatus;
    private final LocalDateTime inboundDate;
    private final Long warehouseId;
    private final Long partnerId;
    private final List<InboundLine> inboundLines;

    private Inbound(Long id, InboundStatus inboundStatus, LocalDateTime inboundDate,
                    Long warehouseId, Long partnerId, List<InboundLine> lines) {
        this.id = id;
        this.inboundStatus = inboundStatus;
        this.inboundDate = inboundDate;
        this.warehouseId = warehouseId;
        this.partnerId = partnerId;
        this.inboundLines = new ArrayList<>(lines);
    }

    public static Inbound create(LocalDateTime inboundDate, Long warehouseId, Long partnerId,
                                 List<InboundLine> lines) {
        validateInboundDate(inboundDate);
        validateWarehouseId(warehouseId);
        validatePartnerId(partnerId);
        validateLines(lines);
        return new Inbound(null, InboundStatus.SCHEDULED, inboundDate, warehouseId, partnerId, lines);
    }

    public static Inbound reconstitute(Long id, InboundStatus inboundStatus, LocalDateTime inboundDate,
                                       Long warehouseId, Long partnerId, List<InboundLine> lines) {
        return new Inbound(id, inboundStatus, inboundDate, warehouseId, partnerId, lines);
    }

    public void receive(Map<Long, Integer> receivedQuantities) {
        if (!this.inboundStatus.canReceive()) {
            throw new InboundInvalidReceiveStatusException(this.inboundStatus.getDescription());
        }
        Set<Long> lineIds = inboundLines.stream().map(InboundLine::getId).collect(Collectors.toSet());
        if (!lineIds.equals(receivedQuantities.keySet())) {
            throw new InboundReceiveLineMismatchException();
        }
        for (InboundLine line : inboundLines) {
            line.receive(receivedQuantities.get(line.getId()));
        }
        this.inboundStatus = InboundStatus.RECEIVED;
    }

    public void cancel() {
        if (!this.inboundStatus.canCancel()) {
            throw new InboundInvalidCancelStatusException(this.inboundStatus.getDescription());
        }
        this.inboundStatus = InboundStatus.CANCELED;
    }

    public List<InboundLine> getInboundLines() {
        return Collections.unmodifiableList(inboundLines);
    }

    private static void validateInboundDate(LocalDateTime inboundDate) {
        if (inboundDate == null) {
            throw new InboundDateRequiredException();
        }
        if (inboundDate.toLocalDate().isBefore(LocalDate.now())) {
            throw new InboundPastDateException();
        }
    }

    private static void validateWarehouseId(Long warehouseId) {
        if (warehouseId == null) {
            throw new InboundWarehouseIdRequiredException();
        }
    }

    private static void validatePartnerId(Long partnerId) {
        if (partnerId == null) {
            throw new InboundPartnerIdRequiredException();
        }
    }

    private static void validateLines(List<InboundLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new InboundLinesRequiredException();
        }
    }
}