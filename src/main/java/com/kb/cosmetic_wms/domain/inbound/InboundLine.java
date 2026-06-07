package com.kb.cosmetic_wms.domain.inbound;

import java.time.LocalDateTime;

public record InboundLine(
        Long productId,
        int quantity,
        LocalDateTime manufactureDate,
        LocalDateTime expirationDate
) {
}
