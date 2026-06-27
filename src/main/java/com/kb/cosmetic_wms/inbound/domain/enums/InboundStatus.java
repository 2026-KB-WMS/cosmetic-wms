package com.kb.cosmetic_wms.inbound.domain.enums;

import lombok.Getter;

@Getter
public enum InboundStatus {
    SCHEDULED("입고 예정"),
    RECEIVED("수령 완료"),
    CANCELED("입고 취소");

    private final String description;

    InboundStatus(String description) {
        this.description = description;
    }

    public boolean canReceive() {
        return this == SCHEDULED;
    }

    public boolean canCancel() {
        return this == SCHEDULED;
    }
}