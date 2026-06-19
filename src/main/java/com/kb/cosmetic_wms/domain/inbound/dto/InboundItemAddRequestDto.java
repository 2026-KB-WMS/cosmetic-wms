package com.kb.cosmetic_wms.domain.inbound.dto;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record InboundItemAddRequestDto(
        @NotNull(message = InboundConstants.INBOUND_PRODUCT_REQUIRED_MESSAGE)
        Long productId,

        @Positive(message = InboundConstants.INVALID_INBOUND_QUANTITY_MESSAGE)
        int quantity,

        @NotNull(message = InboundConstants.MANUFACTURE_DATE_REQUIRED_MESSAGE)
        LocalDateTime manufactureDate,

        @NotNull(message = InboundConstants.EXPIRATION_DATE_REQUIRED_MESSAGE)
        LocalDateTime expirationDate
) {
}
