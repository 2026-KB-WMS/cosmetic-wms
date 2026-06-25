package com.kb.cosmetic_wms.inbound.application.port.in;

import java.time.LocalDateTime;

public record RegisterInboundCommand(Long warehouseId, Long partnerId, LocalDateTime inboundDate) {
}
