package com.kb.cosmetic_wms.lot.application.port.in;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegisterLotCommand(
        LocalDate inboundDate,
        Long inboundId,
        String manufacturerLotNumber,
        LocalDateTime manufacturingDate,
        LocalDateTime expirationDate,
        Long productId
) {
}