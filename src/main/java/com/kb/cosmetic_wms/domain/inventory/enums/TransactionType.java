package com.kb.cosmetic_wms.domain.inventory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {

    // ==== 입고(Inbound) 관련 트랜잭션 ====
    INBOUND_PUTAWAY("입고 실물 적재", true),
    INBOUND_CANCEL("입고 적재 취소", true),

    // ==== 출고(Outbound/Order) 관련 트랜잭션 ====
    ALLOCATE("출고 가용재고 할당", true),
    UNALLOCATE("출고 할당 취소", true),
    PICKING("출고 실물 피킹", true),
    SHIP("최종 출고 완료(소멸)", true),

    // ==== 창고 내부 현장 이동 트랜잭션 ====
    LOCATION_MOVE("창고 내 위치 이동", false),

    // ==== 품질(Quality) 검수 관련 트랜잭션 ====
    QUALITY_INSPECTING("품질 검사 착수", false),
    QUALITY_HOLD("품질 결함 보류(HOLD)", false),
    QUALITY_RELEASE("품질 정상 복구(NORMAL)", false),

    // ==== 폐기 트랜잭션 ====
    DISCARD("실물 폐기 처리", false);

    private final String description;

    // reference_id가 무조건 존재해야 하는 행위인지 여부
    private final boolean isReferenceRequired;
}
