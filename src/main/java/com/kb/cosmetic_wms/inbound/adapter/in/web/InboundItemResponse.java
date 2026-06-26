package com.kb.cosmetic_wms.inbound.adapter.in.web;

import com.kb.cosmetic_wms.inbound.application.port.in.InboundItemResult;
import com.kb.cosmetic_wms.inbound.domain.enums.InspectionStatus;

import java.time.LocalDate;

public record InboundItemResponse(
        Long id,
        Long productId,
        int quantity,
        LocalDate manufactureDate,
        LocalDate expirationDate,
        InspectionStatus inspectionStatus,
        Long lotId,
        Long sectionId
) {
    public static InboundItemResponse from(InboundItemResult result) {
        return new InboundItemResponse(
                result.id(), result.productId(), result.quantity(),
                result.manufactureDate(), result.expirationDate(),
                result.inspectionStatus(), result.lotId(), result.sectionId()
        );
    }
}
