package com.kb.cosmetic_wms.domain.outbound.constants;

public final class OutboundConstants {

    private OutboundConstants() {

    }

    public static final String INVALID_START_PICKING_MESSAGE =
            "출고 대기(PENDING) 상태의 전표만 피킹을 시작할 수 있습니다.";
    public static final String INVALID_SHIP_MESSAGE =
            "피킹 중(PICKING) 상태의 전표만 출고 완료 처리가 가능합니다.";
    public static final String INVALID_CANCEL_MESSAGE =
            "출고 대기(PENDING) 상태의 전표만 취소할 수 있습니다.";

    public static final String ORDER_ID_REQUIRED_MESSAGE =
            "출고 객체 생성 시 발주 ID는 필수 값입니다.";
    public static final String WAREHOUSE_REQUIRED_ID_MESSAGE =
            "출고 객체 생성 시 창고 ID는 필수 값입니다.";
}
