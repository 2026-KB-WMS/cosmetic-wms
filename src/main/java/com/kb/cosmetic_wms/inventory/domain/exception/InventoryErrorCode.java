package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import com.kb.cosmetic_wms.global.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {

    INVENTORY_NOT_FOUND(
            ErrorType.NOT_FOUND,
            "INVENTORY_NOT_FOUND",
            "존재하지 않는 재고입니다."
    ),

    // --- 수량 검증 ---
    INVALID_QUANTITY(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_INVALID_QUANTITY",
            "재고 수량은 음수일 수 없습니다."
    ),
    AVAILABLE_EXCEEDS_TOTAL(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_AVAILABLE_EXCEEDS_TOTAL",
            "출고 가능 수량은 총 재고 수량을 초과할 수 없습니다."
    ),
    INVALID_AVAILABLE_FOR_QUALITY(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_INVALID_AVAILABLE_FOR_QUALITY",
            "비정상 품질 상태의 재고는 출고 가능 수량이 0이어야 합니다."
    ),
    INVALID_ALLOC_QUANTITY(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_INVALID_ALLOC_QUANTITY",
            "할당할 수량은 0보다 커야 합니다."
    ),
    INSUFFICIENT_STOCK(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_INSUFFICIENT_STOCK",
            "가용 재고가 부족하여 할당할 수 없습니다."
    ),
    QUANTITY_EXCEEDS_STOCK(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_QUANTITY_EXCEEDS_STOCK",
            "변경 요청 수량이 현재 보유한 재고 수량을 초과할 수 없습니다."
    ),

    // --- 상태 전환 규칙 ---
    NOT_ALLOCATED(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_NOT_ALLOCATED",
            "할당된 재고만 할당 취소할 수 있습니다."
    ),
    ALLOCATED_CANNOT_MOVE(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_ALLOCATED_CANNOT_MOVE",
            "이미 할당된 재고는 이동(MOVING) 시킬 수 없습니다. 할당 취소부터 진행해주세요."
    ),
    ALREADY_MOVING(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_ALREADY_MOVING",
            "이미 이동 중인 재고입니다."
    ),
    NOT_MOVING(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_NOT_MOVING",
            "이동 중(MOVING) 상태의 재고만 이동 완료 처리가 가능합니다."
    ),
    ALLOCATED_CANNOT_INSPECT(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_ALLOCATED_CANNOT_INSPECT",
            "이미 할당된 재고는 검수(INSPECTING) 상태로 변경할 수 없습니다. 할당 취소부터 진행해주세요."
    ),
    ALLOCATED_CANNOT_CHANGE_QUALITY(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_ALLOCATED_CANNOT_CHANGE_QUALITY",
            "할당된 재고는 품질 상태를 변경할 수 없습니다."
    ),
    ALLOCATED_CANNOT_HOLD(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_ALLOCATED_CANNOT_HOLD",
            "이미 할당된 재고는 출고 금지(HOLD) 처리할 수 없습니다. 할당 취소부터 진행해주세요."
    ),
    ALLOCATED_CANNOT_DISCARD(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_ALLOCATED_CANNOT_DISCARD",
            "할당된 재고는 폐기 처리할 수 없습니다. 할당 취소부터 진행해주세요."
    ),

    // --- 병합 규칙 ---
    INCOMPATIBLE_MERGE_KEY(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_INCOMPATIBLE_MERGE_KEY",
            "상품·로트·섹션이 동일한 재고만 병합 가능합니다."
    ),
    INCOMPATIBLE_MERGE_STATUS(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_INCOMPATIBLE_MERGE_STATUS",
            "동일 상태의 재고만 병합 가능합니다."
    ),

    // --- 상태 조합 검증 (InventoryStatusSet) ---
    INVALID_STATUS_NULL(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_INVALID_STATUS_NULL",
            "재고의 모든 상태값은 필수입니다."
    ),
    ALLOCATED_NON_NORMAL_QUALITY(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_ALLOCATED_NON_NORMAL_QUALITY",
            "할당 또는 출고 완료된 재고는 품질 상태가 정상이어야 합니다."
    ),
    MOVING_WITH_ALLOCATED(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_MOVING_WITH_ALLOCATED",
            "이미 가맹점 주문 처리 중인 재고는 창고 간 이동(MOVING)을 할 수 없습니다."
    ),
    MOVING_WITH_NON_NORMAL_QUALITY(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_MOVING_WITH_NON_NORMAL_QUALITY",
            "결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다."
    ),
    DOCKING_WITH_ALLOCATED(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_DOCKING_WITH_ALLOCATED",
            "도킹 구역 대기 중인 재고는 할당 대상이 아닙니다."
    ),
    DOCKING_CANNOT_MOVE(
            ErrorType.RULE_VIOLATION,
            "INVENTORY_DOCKING_CANNOT_MOVE",
            "도킹 구역 대기 중인 재고는 이동(MOVING) 상태로 전환할 수 없습니다."
    ),
    INVALID_AVAILABLE_FOR_DOCKING(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_INVALID_AVAILABLE_FOR_DOCKING",
            "도킹 구역 대기 중인 재고의 출고 가능 수량은 0이어야 합니다."
    ),

    // --- 재고 이력 검증 (InventoryTransaction) ---
    TRANSACTION_INVENTORY_ID_REQUIRED(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_TRANSACTION_INVENTORY_ID_REQUIRED",
            "재고 식별자(ID)는 필수입니다."
    ),
    TRANSACTION_TYPE_REQUIRED(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_TRANSACTION_TYPE_REQUIRED",
            "트랜잭션 타입은 필수입니다."
    ),
    TRANSACTION_STATUS_REQUIRED(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_TRANSACTION_STATUS_REQUIRED",
            "현재 재고 상태 정보는 필수입니다."
    ),
    TRANSACTION_MEMBER_ID_REQUIRED(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_TRANSACTION_MEMBER_ID_REQUIRED",
            "작업자 식별자(ID)는 필수입니다."
    ),
    TRANSACTION_INVALID_QUANTITY(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_TRANSACTION_INVALID_QUANTITY",
            "트랜잭션 변동 수량은 0보다 커야 합니다."
    ),
    TRANSACTION_REFERENCE_REQUIRED(
            ErrorType.INVALID_REQUEST,
            "INVENTORY_TRANSACTION_REFERENCE_REQUIRED",
            "원인 전표 ID가 필수입니다."
    );

    private final ErrorType type;
    private final String code;
    private final String message;
}