package com.kb.cosmetic_wms.inbound.adapter.in.web;

import jakarta.validation.constraints.NotNull;

public record InboundPutawayRequest(
        @NotNull(message = "적재 시 생성된 로트(Lot) 정보는 필수입니다.") Long lotId,
        @NotNull(message = "적재될 섹션 정보는 필수입니다.") Long sectionId
) {
}
