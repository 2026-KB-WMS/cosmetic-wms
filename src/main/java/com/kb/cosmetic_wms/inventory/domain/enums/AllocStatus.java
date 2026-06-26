package com.kb.cosmetic_wms.inventory.domain.enums;

import lombok.Getter;

@Getter
public enum AllocStatus {
    UNALLOCATED("미할당", "출고(발주)에 사용 가능한 상태"),
    ALLOCATED("할당됨", "특정 발주 건에 묶여 출고 대기 중인 상태"),
    SHIPPED("출고 완료", "창고를 떠나 가맹점으로 배송된 상태");

    private final String description;
    private final String remarks;

    AllocStatus(String description, String remarks) {
        this.description = description;
        this.remarks = remarks;
    }
}