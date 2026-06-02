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

    public static final String START_INSPECTING_FOR_UNALLOCATED_ONLY_MESSAGE =
            "이미 할당된 재고는 검수(INSPECTING) 상태로 변경할 수 없습니다. 할당 취소부터 진행해주세요.";

    public static final String CHANGE_QUALITY_FOR_UNALLOCATED_ONLY_MESSAGE =
            "할당된 재고는 품질 상태를 변경할 수 없습니다.";

    public static final String HOLD_FOR_UNALLOCATED_ONLY_MESSAGE =
            "이미 할당된 재고는 출고 금지(HOLD) 처리할 수 없습니다. 할당 취소부터 진행해주세요.";

    public static final String DISCARD_FOR_UNALLOCATED_ONLY_MESSAGE =
            "할당된 재고는 폐기 처리할 수 없습니다. 할당 취소부터 진행해주세요.";
}
