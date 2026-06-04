package com.kb.cosmetic_wms.domain.order.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("발주 대기"),
    IN_PROGRESS("작업 중"),
    SHIPPED("배송 중"),
    DELIVERED("배송 완료"),
    CANCELED("발주 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
