package com.kb.cosmetic_wms.domain.partner.enums;

import lombok.Getter;

@Getter
public enum PartnerType {
    VENDOR("외부 공급사(브랜드사)"),
    HEADQUARTER("자사 본사"),
    BRANCH("타 물류 창고/지점");

    private final String description;

    PartnerType(String description) {
        this.description = description;
    }
}
