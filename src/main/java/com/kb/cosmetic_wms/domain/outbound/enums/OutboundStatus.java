package com.kb.cosmetic_wms.domain.outbound.enums;

import lombok.Getter;

@Getter
public enum OutboundStatus {
    PENDING("출고 대기"),
    PICKING("피킹 중"),
    SHIPPED("출고 완료(배송 중)"),
    CANCELED("출고 취소");

    private final String description;

    OutboundStatus(String description) {
        this.description = description;
    }
}
