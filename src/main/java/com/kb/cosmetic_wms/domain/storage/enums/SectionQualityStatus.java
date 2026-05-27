package com.kb.cosmetic_wms.domain.storage.enums;

import lombok.Getter;

@Getter
public enum SectionQualityStatus {
    INSPECTING("검수 대기"),
    NORMAL("정상"),
    HOLD("출고 금지/폐기 예정");

    private final String description;

    SectionQualityStatus(String description) {
        this.description = description;
    }
}
