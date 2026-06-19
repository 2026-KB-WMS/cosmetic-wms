package com.kb.cosmetic_wms.domain.inbound.dto;

import com.kb.cosmetic_wms.domain.inbound.entity.InboundItem;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;

import java.time.LocalDate;

public record InboundItemResponseDto(
        Long id,
        Long productId,
        int quantity,
        LocalDate manufactureDate,
        LocalDate expirationDate,
        InspectionStatus inspectionStatus,
        Long lotId,
        Long sectionId
) {
    public static InboundItemResponseDto from(InboundItem item) {
        return new InboundItemResponseDto(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getManufactureDate(),
                item.getExpirationDate(),
                item.getInspectionStatus(),
                item.getLotId(),
                item.getSectionId()
        );
    }
}
