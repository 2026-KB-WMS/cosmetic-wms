package com.kb.cosmetic_wms.inbound.domain.constants;

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

    public static final String INVALID_ADD_ITEM_MESSAGE =
            "입고 예정(SCHEDULED) 상태일 때만 품목을 추가할 수 있습니다.";

    public static final String INCOMPLETE_INSPECTION_MESSAGE =
            "아직 적재가 완료되지 않았거나 검수 중인 품목이 존재하여 입고 완료 처리가 불가능합니다.";

    public static final String INVALID_INBOUND_QUANTITY_MESSAGE =
            "입고 예정 수량은 0보다 커야 합니다.";

    public static final String INBOUND_MASTER_REQUIRED_MESSAGE =
            "입고 마스터 객체는 필수입니다.";
    public static final String INBOUND_PRODUCT_REQUIRED_MESSAGE =
            "입고 상품 정보는 필수입니다.";

    public static final String PUTAWAY_LOT_REQUIRED_MESSAGE =
            "적재 시 생성된 로트(Lot) 정보는 필수입니다.";
    public static final String PUTAWAY_SECTION_REQUIRED_MESSAGE =
            "적재될 섹션 정보는 필수입니다.";

    public static final String INVALID_PUTAWAY_STATUS_MESSAGE =
            "이미 적재가 완료되었거나 검수가 진행된 품목입니다. (현재 상태: %s)";
    public static final String MANUFACTURE_DATE_REQUIRED_MESSAGE =
            "제조일자는 필수입니다.";
    public static final String EXPIRATION_DATE_REQUIRED_MESSAGE =
            "유통기한은 필수입니다.";

    public static final String INVALID_NORMAL_STATUS_MESSAGE =
            "검수 중 상태에서만 정상 완료 처리가 가능합니다.";
    public static final String INVALID_HOLD_STATUS_MESSAGE =
            "검수 중 상태에서만 검수 보류 처리가 가능합니다.";
}
