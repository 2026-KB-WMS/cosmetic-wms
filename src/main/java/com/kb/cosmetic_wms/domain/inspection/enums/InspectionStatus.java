package com.kb.cosmetic_wms.domain.inspection.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InspectionStatus {
    WAITING("검사 대기"),
    IN_PROGRESS("검사 진행 중"),
    COMPLETED("검사 완료");

    private final String description;
}
