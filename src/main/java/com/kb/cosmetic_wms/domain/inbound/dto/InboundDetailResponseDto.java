package com.kb.cosmetic_wms.domain.inbound.dto;

import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.enums.InboundStatus;

import java.time.LocalDateTime;
import java.util.List;

public record InboundDetailResponseDto(
        Long id,
        Long warehouseId,
        Long partnerId,
        InboundStatus inboundStatus,
        LocalDateTime inboundDate,
        List<InboundItemResponseDto> items
) {
    public static InboundDetailResponseDto from(Inbound inbound) {
        return new InboundDetailResponseDto(
                inbound.getId(),
                inbound.getWarehouseId(),
                inbound.getPartnerId(),
                inbound.getInboundStatus(),
                inbound.getInboundDate(),
                inbound.getInboundItems().stream()
                        .map(InboundItemResponseDto::from)
                        .toList()
        );
    }
}
