package com.kb.cosmetic_wms.inbound.application.port.in;

import java.time.LocalDate;

public record AddInboundItemCommand(Long productId, int quantity, LocalDate manufactureDate, LocalDate expirationDate) {
}
