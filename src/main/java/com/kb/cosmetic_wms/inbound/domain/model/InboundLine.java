package com.kb.cosmetic_wms.inbound.domain.model;

import java.time.LocalDate;

public record InboundLine(
        Long productId,
        int quantity,
        LocalDate manufactureDate,
        LocalDate expirationDate
) {
}
