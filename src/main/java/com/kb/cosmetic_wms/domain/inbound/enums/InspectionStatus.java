package com.kb.cosmetic_wms.domain.inbound.enums;

import lombok.Getter;

@Getter
public enum InspectionStatus {
    WAITING("검수 대기"),
    INSPECTING("검수 중"),
    NORMAL("검수 완료(정상)"),
    HOLD("검수 보류");

    private final String description;

    InspectionStatus(String description) {
        this.description = description;
    }
}
