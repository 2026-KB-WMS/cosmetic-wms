package com.kb.cosmetic_wms.lot.application.port.in;

import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import com.kb.cosmetic_wms.lot.domain.model.Lot;

import java.time.LocalDateTime;

public record LotResult(
        Long id,
        String lotNumber,
        LocalDateTime manufacturingDate,
        LocalDateTime expirationDate,
        LotStatus status,
        Long productId
) {
    public static LotResult from(Lot lot) {
        return new LotResult(
                lot.getId(),
                lot.getLotNumber(),
                lot.getManufacturingDate(),
                lot.getExpirationDate(),
                lot.getStatus(),
                lot.getProductId()
        );
    }
}