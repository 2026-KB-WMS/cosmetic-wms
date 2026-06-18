package com.kb.cosmetic_wms.domain.lot.dto;

import com.kb.cosmetic_wms.domain.lot.entity.Lot;
import com.kb.cosmetic_wms.domain.lot.enums.LotStatus;

import java.time.LocalDateTime;

public record LotDetailResponseDto(
        Long id,
        String lotNumber,
        LocalDateTime manufacturingDate,
        LocalDateTime expirationDate,
        LotStatus status,
        Long productId
) {
    public static LotDetailResponseDto from(Lot lot) {
        return new LotDetailResponseDto(
                lot.getId(),
                lot.getLotNumber(),
                lot.getManufacturingDate(),
                lot.getExpirationDate(),
                lot.getStatus(),
                lot.getProductId()
        );
    }
}
