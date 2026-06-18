package com.kb.cosmetic_wms.domain.inventory.constants;

public final class InventoryConstants {

    private InventoryConstants() {
    }

    public static final String INVALID_QUANTITY_MESSAGE =
            "재고 수량은 음수일 수 없습니다.";

    public static final String OVER_AVAILABLE_QUANTITY_MESSAGE =
            "출고 가능 수량은 총 재고 수량을 초과할 수 없습니다.";

    public static final String INVALID_ALLOCATE_QUANTITY_MESSAGE =
            "할당할 수량은 0보다 커야 합니다.";

    public static final String LACK_OF_AVAILABLE_QUANTITY_MESSAGE =
            "가용 재고가 부족하여 할당할 수 없습니다.";

    public static final String INVALID_QUALITY_AVAILABLE_QUANTITY_MESSAGE =
            "품질 상태가 %s(%s)일 경우 출고 가능 수량은 0이어야 합니다.";

    public static final String EXCEED_INVENTORY_QUANTITY_MESSAGE =
            "변경 요청 수량이 현재 보유한 재고 수량을 초과할 수 없습니다.";

    // StatusSet 관련 상수
    public static final String STATUS_SET_REQUIRED_MESSAGE =
            "재고의 모든 상태값은 필수입니다.";

    public static final String INVALID_STATUS_SET_QUALITY_MESSAGE =
            "할당 또는 출고 완료된 재고는 품질 상태가 정상이어야 합니다.";

    public static final String INVALID_STATUS_SET_ALLOC_MOVING_MESSAGE =
            "이미 가맹점 주문 처리 중인 재고는 창고 간 이동(MOVING)을 할 수 없습니다.";

    public static final String INVALID_STATUS_SET_QUALITY_MOVING_MESSAGE =
            "품질 상태가 %s인 결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다.";

    public static final String UNALLOCATE_FOR_ALLOCATED_ONLY_MESSAGE =
            "할당된 재고만 할당 취소할 수 있습니다.";

    public static final String START_MOVING_FOR_UNALLOCATED_ONLY_MESSAGE =
            "이미 할당된 재고는 이동(MOVING) 시킬 수 없습니다. 할당 취소부터 진행해주세요.";

    public static final String ALREADY_MOVING_INVENTORY_MESSAGE =
            "이미 이동 중인 재고입니다.";

    public static final String FINISH_MOVING_FOR_MOVING_ONLY_MESSAGE =
            "이동 중(MOVING) 상태의 재고만 이동 완료 처리가 가능합니다.";

    public static final String START_INSPECTING_FOR_UNALLOCATED_ONLY_MESSAGE =
            "이미 할당된 재고는 검수(INSPECTING) 상태로 변경할 수 없습니다. 할당 취소부터 진행해주세요.";

    public static final String CHANGE_QUALITY_FOR_UNALLOCATED_ONLY_MESSAGE =
            "할당된 재고는 품질 상태를 변경할 수 없습니다.";

    public static final String HOLD_FOR_UNALLOCATED_ONLY_MESSAGE =
            "이미 할당된 재고는 출고 금지(HOLD) 처리할 수 없습니다. 할당 취소부터 진행해주세요.";

    public static final String DISCARD_FOR_UNALLOCATED_ONLY_MESSAGE =
            "할당된 재고는 폐기 처리할 수 없습니다. 할당 취소부터 진행해주세요.";

    public static final String MERGE_STATUS_MISMATCH_MESSAGE =
            "동일 상태의 재고만 병합 가능합니다.";

    // ==== 재고 트랜잭션 이력(InventoryTransaction) 전용 예외 상수 ====
    public static final String INVENTORY_ID_REQUIRED_MESSAGE =
            "재고 식별자(ID)는 필수입니다.";
    public static final String TRANSACTION_TYPE_REQUIRED_MESSAGE =
            "트랜잭션 타입은 필수입니다.";
    public static final String CURR_STATUS_REQUIRED_MESSAGE =
            "현재 재고 상태 정보는 필수입니다.";
    public static final String MEMBER_ID_REQUIRED_MESSAGE =
            "작업자 식별자(ID)는 필수입니다.";
    public static final String INVALID_TRANSACTION_QTY_MESSAGE =
            "트랜잭션 변동 수량은 0보다 커야 합니다.";
    public static final String REFERENCE_ID_REQUIRED_TEMPLATE =
            "%s 행위는 원인 전표 ID가 필수입니다.";

}
