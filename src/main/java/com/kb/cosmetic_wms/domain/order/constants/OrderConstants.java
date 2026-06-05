package com.kb.cosmetic_wms.domain.order.constants;

public final class OrderConstants {
    private OrderConstants() {
    }

    public static final String STORE_REQUIRED_MESSAGE = "발주 시 가맹점 정보는 필수입니다.";
    public static final String WAREHOUSE_REQUIRED_MESSAGE = "발주 시 창고 정보는 필수입니다.";
    public static final String ORDER_ITEM_MINIMUM_MESSAGE = "발주 항목은 최소 1개 이상이어야 합니다.";

    public static final String INVALID_CANCEL_STATUS_MESSAGE =
            "발주 대기(PENDING) 상태에서만 취소가 가능합니다.";
    public static final String INVALID_START_STATUS_MESSAGE =
            "발주 대기(PENDING) 상태에서만 작업을 시작할 수 있습니다.";
    public static final String INVALID_SHIP_STATUS_MESSAGE =
            "작업 중(IN_PROGRESS) 상태에서만 배송을 시작할 수 있습니다.";
    public static final String INVALID_DELIVERY_STATUS_MESSAGE =
            "배송 중(SHIPPED) 상태에서만 배송 완료 처리가 가능합니다.";
}
