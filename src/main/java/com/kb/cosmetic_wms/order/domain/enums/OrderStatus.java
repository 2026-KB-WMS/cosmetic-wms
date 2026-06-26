package com.kb.cosmetic_wms.order.domain.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("발주 신청"),
    CONFIRMED("발주 확정"),
    PREPARING("배송 준비 중"),
    SHIPPED("배송 중"),
    DELIVERED("배송 완료"),
    CANCELED("발주 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
