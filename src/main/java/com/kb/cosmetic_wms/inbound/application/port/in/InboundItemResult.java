package com.kb.cosmetic_wms.inbound.application.port.in;

import com.kb.cosmetic_wms.inbound.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inbound.domain.model.InboundItem;

import java.time.LocalDate;

public record InboundItemResult(
        Long id,
        Long productId,
        int quantity,
        LocalDate manufactureDate,
        LocalDate expirationDate,
        InspectionStatus inspectionStatus,
        Long lotId,
        Long sectionId
) {
    public static InboundItemResult from(InboundItem item) {
        return new InboundItemResult(
                item.getId(), item.getProductId(), item.getQuantity(),
                item.getManufactureDate(), item.getExpirationDate(),
                item.getInspectionStatus(), item.getLotId(), item.getSectionId()
        );
    }
}
