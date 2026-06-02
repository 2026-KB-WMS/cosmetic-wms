package com.kb.cosmetic_wms.domain.inbound.constants;

public final class InboundConstants {

    private InboundConstants() {
    }

    public static final String PAST_INBOUND_DATE_MESSAGE =
            "입고 예정일은 현재 날짜보다 과거일 수 없습니다.";
    public static final String DATE_REQUIRED_MESSAGE =
            "입고 예정일은 필수입니다.";
    public static final String WAREHOUSE_REQUIRED_MESSAGE =
            "입고 창고 정보는 필수입니다.";
    public static final String PARTNER_REQUIRED_MESSAGE =
            "입고 파트너 정보는 필수입니다.";

    public static final String INVALID_START_STATUS_MESSAGE =
            "입고 예정 상태에서만 작업을 시작할 수 있습니다. (현재 상태: %s)";
    public static final String INVALID_COMPLETE_STATUS_MESSAGE =
            "작업이 진행 중인 상태에서만 입고 완료 처리가 가능합니다. (현재 상태: %s)";
    public static final String INVALID_CANCEL_STATUS_MESSAGE =
            "이미 작업이 진행되었거나 완료된 입고 건은 취소할 수 없습니다. (현재 상태: %s)";

    public static final String INVALID_INBOUND_QUANTITY_MESSAGE =
            "입고 예정 수량은 0보다 커야 합니다.";
}
