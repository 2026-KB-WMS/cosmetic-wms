package com.kb.cosmetic_wms.storage.domain.model;

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
