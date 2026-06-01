package com.kb.cosmetic_wms.domain.inventory.enums;

public enum LotStatus {
    AVAILABLE("정상(가용)"),
    HOLD("보류"),
    EXPIRED("유통기한 만료"),
    RECALLED("리콜 대상"),
    DAMAGED("파손"),
    DISPOSED("폐기");

    private final String description;

    LotStatus(String description) {
        this.description = description;
    }
}
