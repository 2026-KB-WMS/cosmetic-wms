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

    // StatusSet 관련 상수
    public static final String STATUS_SET_REQUIRED_MESSAGE =
            "재고의 모든 상태값은 필수입니다.";

    public static final String INVALID_STATUS_SET_QUALITY_MESSAGE =
            "할당 또는 출고 완료된 재고는 품질 상태가 정상이어야 합니다.";

    public static final String INVALID_STATUS_SET_ALLOC_MOVING_MESSAGE =
            "이미 가맹점 주문 처리 중인 재고는 창고 간 이동(MOVING)을 할 수 없습니다.";

    public static final String INVALID_STATUS_SET_QUALITY_MOVING_MESSAGE =
            "품질 상태가 %s인 결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다.";
}
