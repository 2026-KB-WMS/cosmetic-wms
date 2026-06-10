package com.kb.cosmetic_wms.domain.inspection.constants;

public final class QualityConstants {

    private QualityConstants() {
    }

    public static final String INBOUND_ITEM_ID_REQUIRED_MESSAGE = "품질 검사 대상 입고 품목 식별자(ID)는 필수입니다.";
    public static final String INVENTORY_ID_REQUIRED_MESSAGE = "품질 검사 대상 재고 식별자(ID)는 필수입니다.";
    public static final String INSPECTOR_ID_REQUIRED_MESSAGE = "품질 검사자 식별자(ID)는 필수입니다.";
    public static final String INSPECTION_RESULT_REQUIRED_MESSAGE = "품질 검사 결과는 필수입니다.";

    public static final String DEFECT_REASON_REQUIRED_MESSAGE = "품질 검사 결과가 불합격일 경우 부적합 사유는 필수입니다.";
}
