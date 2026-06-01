package com.kb.cosmetic_wms.domain.inventory.enums;

import lombok.Getter;

@Getter
public enum LocStatus {
    STORED("보관 중", "창고 내 정상 보관 상태"),
    MOVING("이동 중", "재고 이동 중");

    private final String description;
    private final String remarks;

    LocStatus(String description, String remarks) {
        this.description = description;
        this.remarks = remarks;
    }
}
