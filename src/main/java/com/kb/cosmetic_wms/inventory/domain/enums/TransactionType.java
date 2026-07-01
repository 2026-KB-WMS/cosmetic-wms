package com.kb.cosmetic_wms.inventory.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {

    ALLOCATE("출고 가용재고 할당", true),
    UNALLOCATE("출고 할당 취소", true),
    SHIP("최종 출고 완료(소멸)", true),

    LOCATION_MOVE("창고 내 위치 이동", false),

    INSPECTION_PASS("품질 검사 합격 재고 등록", true),
    INSPECTION_FAIL("품질 검사 불합격 재고 등록", true),
    QUALITY_INSPECTING("품질 검사 착수", false),
    QUALITY_HOLD("품질 결함 보류(HOLD)", false),
    QUALITY_RELEASE("품질 정상 복구(NORMAL)", false),

    DISCARD("실물 폐기 처리", false),

    SPLIT_DEDUCT("재고 분할 — 원본 수량 차감", false);

    private final String description;
    private final boolean isReferenceRequired;
}