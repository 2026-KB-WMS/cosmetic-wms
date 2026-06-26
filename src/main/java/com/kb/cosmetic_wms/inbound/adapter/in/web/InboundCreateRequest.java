package com.kb.cosmetic_wms.inbound.adapter.in.web;

import com.kb.cosmetic_wms.inbound.domain.constants.InboundConstants;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record InboundCreateRequest(
        @NotNull(message = InboundConstants.WAREHOUSE_REQUIRED_MESSAGE) Long warehouseId,
        @NotNull(message = InboundConstants.PARTNER_REQUIRED_MESSAGE) Long partnerId,
        @NotNull(message = InboundConstants.DATE_REQUIRED_MESSAGE) LocalDateTime inboundDate
) {
}
