package com.kb.cosmetic_wms.inbound.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record InboundItemAddRequest(
        @NotNull(message = "입고 상품 정보는 필수입니다.") Long productId,
        @Positive(message = "입고 예정 수량은 0보다 커야 합니다.") int quantity,
        @NotNull(message = "제조일자는 필수입니다.") LocalDate manufactureDate,
        @NotNull(message = "유통기한은 필수입니다.") LocalDate expirationDate
) {
}
