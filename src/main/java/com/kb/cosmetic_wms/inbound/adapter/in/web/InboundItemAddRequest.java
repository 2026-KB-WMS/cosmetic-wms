package com.kb.cosmetic_wms.inbound.adapter.in.web;

import com.kb.cosmetic_wms.inbound.domain.constants.InboundConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record InboundItemAddRequest(
        @NotNull(message = InboundConstants.INBOUND_PRODUCT_REQUIRED_MESSAGE) Long productId,
        @Positive(message = InboundConstants.INVALID_INBOUND_QUANTITY_MESSAGE) int quantity,
        @NotNull(message = InboundConstants.MANUFACTURE_DATE_REQUIRED_MESSAGE) LocalDate manufactureDate,
        @NotNull(message = InboundConstants.EXPIRATION_DATE_REQUIRED_MESSAGE) LocalDate expirationDate
) {
}
