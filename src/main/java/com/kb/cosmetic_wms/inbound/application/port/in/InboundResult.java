package com.kb.cosmetic_wms.inbound.application.port.in;

import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;

import java.time.LocalDateTime;
import java.util.List;

public record InboundResult(
        Long id,
        Long warehouseId,
        Long partnerId,
        InboundStatus inboundStatus,
        LocalDateTime inboundDate,
        List<InboundItemResult> items
) {
    public static InboundResult from(Inbound inbound) {
        return new InboundResult(
                inbound.getId(),
                inbound.getWarehouseId(),
                inbound.getPartnerId(),
                inbound.getInboundStatus(),
                inbound.getInboundDate(),
                inbound.getInboundItems().stream().map(InboundItemResult::from).toList()
        );
    }
}
