package com.kb.cosmetic_wms.domain.outbound.constants;

public final class OutboundConstants {

    private OutboundConstants() {
    }

    public static final String INVALID_ADD_ITEM_STATUS_MESSAGE =
            "출고 대기(PENDING) 상태일 때만 품목을 추가할 수 있습니다.";
    public static final String INCOMPLETE_PICKING_MESSAGE =
            "모든 출고 품목의 피킹이 완료되지 않아 출고 확정을 할 수 없습니다.";

    // OutboundItem
    public static final String INVALID_TARGET_QUANTITY_MESSAGE =
            "출고 지시 수량은 0 이하일 수 없습니다.";
    public static final String EXCEED_PICKED_QUANTITY_MESSAGE =
            "실제 피킹 수량은 출고 지시 수량을 초과할 수 없습니다.";
    public static final String INVALID_PICKED_QUANTITY_MESSAGE =
            "실제 피킹 수량은 음수일 수 없습니다.";
    public static final String INVALID_PICKING_STATUS_MESSAGE =
            "피킹 수량 변경은 출고 준비 중(PROCESSING) 상태에서만 가능합니다.";
}