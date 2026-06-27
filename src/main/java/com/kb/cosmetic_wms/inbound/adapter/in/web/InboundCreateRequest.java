package com.kb.cosmetic_wms.inbound.adapter.in.web;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record InboundCreateRequest(
        @NotNull(message = "입고 창고 정보는 필수입니다.") Long warehouseId,
        @NotNull(message = "입고 파트너 정보는 필수입니다.") Long partnerId,
        @NotNull(message = "입고 예정일은 필수입니다.") LocalDateTime inboundDate
) {
}
