package com.kb.cosmetic_wms.lot.adapter.in.web;

import com.kb.cosmetic_wms.lot.application.port.in.LotResult;
import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;

import java.time.LocalDateTime;

public record LotDetailResponse(
        Long id,
        Long inboundId,
        String manufacturerLotNumber,
        LocalDateTime manufacturingDate,
        LocalDateTime expirationDate,
        LotStatus status,
        Long productId
) {
    public static LotDetailResponse from(LotResult result) {
        return new LotDetailResponse(
                result.id(),
                result.inboundId(),
                result.manufacturerLotNumber(),
                result.manufacturingDate(),
                result.expirationDate(),
                result.status(),
                result.productId()
        );
    }
}
