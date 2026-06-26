package com.kb.cosmetic_wms.inbound.domain.enums;

import lombok.Getter;

@Getter
public enum InboundStatus {
    SCHEDULED("입고 예정"),
    IN_PROGRESS("입고 진행 중"),
    COMPLETED("입고 완료"),
    CANCELED("입고 취소");

    private final String description;

    InboundStatus(String description) {
        this.description = description;
    }
}
