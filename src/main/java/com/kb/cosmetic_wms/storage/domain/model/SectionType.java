package com.kb.cosmetic_wms.storage.domain.model;

import lombok.Getter;

@Getter
public enum SectionType {
    DOCKING("DOCK", "반입/반출(검수) 구역"),
    STORAGE("STR", "일반 보관 구역"),
    QUARANTINE("QUAR", "격리/폐기 구역");

    private final String shortCode;
    private final String description;

    SectionType(String shortCode, String description) {
        this.shortCode = shortCode;
        this.description = description;
    }
}
