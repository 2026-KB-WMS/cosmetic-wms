package com.kb.cosmetic_wms.domain.inventory.enums;

import lombok.Getter;

@Getter
public enum QualityStatus {
    NORMAL("정상", "판매 및 사용 가능"),
    INSPECTING("검수 대기/중", "반품 회수 후 또는 입고 시 품질 확인 중"),
    HOLD("출고 금지", "리콜 대상이거나 품질에 문제가 발생한 상태"),
    DISCARD_SCHEDULED("폐기 예정", "검수 결과 불량으로 판정되어 폐기 대기 중");

    private final String description;
    private final String remarks;

    QualityStatus(String description, String remarks) {
        this.description = description;
        this.remarks = remarks;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }
}
