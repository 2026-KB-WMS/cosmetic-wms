package com.kb.cosmetic_wms.domain.inbound;

import java.time.LocalDate;

public record InboundLine(
        Long productId,
        int quantity,
        LocalDate manufactureDate,
        LocalDate expirationDate
) {
}
