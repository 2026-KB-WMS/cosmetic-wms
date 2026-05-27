package com.kb.cosmetic_wms.domain.storage.enums;

import lombok.Getter;

@Getter
public enum SectionAllocationStatus {
    NONE("-"),
    AVAILABLE("가용"),
    EXCLUDED("할당 제외");

    private final String description;

    SectionAllocationStatus(String description) {
        this.description = description;
    }
}
