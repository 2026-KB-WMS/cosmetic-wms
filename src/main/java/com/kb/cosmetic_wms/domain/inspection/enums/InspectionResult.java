package com.kb.cosmetic_wms.domain.inspection.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InspectionResult {
    PASSED("합격"),
    FAILED("불합격");

    private final String description;
}
