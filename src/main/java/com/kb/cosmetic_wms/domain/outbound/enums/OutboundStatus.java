package com.kb.cosmetic_wms.domain.outbound.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutboundStatus {
    PENDING("출고 대기"),
    ALLOCATED("재고 할당"),
    PROCESSING("출고 준비 중"),
    SHIPPED("출하 완료"),
    CANCELED("출고 취소");

    private final String description;
}
