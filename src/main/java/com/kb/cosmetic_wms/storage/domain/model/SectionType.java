package com.kb.cosmetic_wms.storage.domain.model;

import lombok.Getter;

@Getter
public enum SectionType {
    DOCKING("DOCK", "반입/반출(검수) 구역"),
    HIGH_ROT("HIGH", "고회전 보관 구역"),
    MID_ROT("MID", "중회전 보관 구역"),
    LOW_ROT("LOW", "저회전 보관 구역"),
    QUARANTINE("QUAR", "격리/폐기 구역");

    private final String shortCode;
    private final String description;

    SectionType(String shortCode, String description) {
        this.shortCode = shortCode;
        this.description = description;
    }
}
