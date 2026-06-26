package com.kb.cosmetic_wms.inspection.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InspectionSourceType {
    INBOUND("입고"),
    RETURN("반품");

    private final String description;
}
