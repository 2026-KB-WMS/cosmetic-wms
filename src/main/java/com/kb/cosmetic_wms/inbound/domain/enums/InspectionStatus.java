package com.kb.cosmetic_wms.inbound.domain.enums;

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

    public boolean canPutaway() {
        return this == WAITING;
    }

    public boolean canApprove() {
        return this == INSPECTING;
    }

    public boolean canHold() {
        return this == INSPECTING;
    }
}
