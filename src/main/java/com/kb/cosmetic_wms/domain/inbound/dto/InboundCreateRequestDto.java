package com.kb.cosmetic_wms.domain.inbound.dto;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record InboundCreateRequestDto(
        @NotNull(message = InboundConstants.WAREHOUSE_REQUIRED_MESSAGE)
        Long warehouseId,

        @NotNull(message = InboundConstants.PARTNER_REQUIRED_MESSAGE)
        Long partnerId,

        @NotNull(message = InboundConstants.DATE_REQUIRED_MESSAGE)
        LocalDateTime inboundDate
) {
}
