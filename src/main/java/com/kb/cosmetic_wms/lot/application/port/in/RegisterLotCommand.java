package com.kb.cosmetic_wms.lot.application.port.in;

import java.time.LocalDateTime;

public record RegisterLotCommand(
        Long inboundId,
        String manufacturerLotNumber,
        LocalDateTime manufacturingDate,
        LocalDateTime expirationDate,
        Long productId
) {
}
