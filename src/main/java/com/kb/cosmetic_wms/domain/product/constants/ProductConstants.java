package com.kb.cosmetic_wms.domain.product.constants;

public final class ProductConstants {

    private ProductConstants() {
    }

    // --- SKU 관련 상수 ---
    public static final String SKU_FORMAT = "%s-%s-%s-%d-%04d";
    public static final int SEQUENCE_MIN_BOUND = 1;
    public static final int SEQUENCE_MAX_BOUND = 9999;
    public static final int MIN_PRICE_BOUND = 0;

    // Validation Messages
    public static final String BRAND_NAME_REQUIRED_MESSAGE =
            "브랜드명은 필수 입력 항목입니다.";
    public static final String PRODUCT_NAME_REQUIRED_MESSAGE =
            "상품명은 필수 입력 항목입니다.";
    public static final String INVALID_PRODUCT_PRICE_MESSAGE =
            "상품 가격은 " + MIN_PRICE_BOUND + "원 이상이어야 합니다.";
    public static final String TEMPERATURE_TYPE_REQUIRED_MESSAGE =
            "보관 온도 타입은 필수 입력 항목입니다.";

    public static final String INVALID_SEQUENCE_MIN_MESSAGE =
            "SKU 일련번호 순번은 " + SEQUENCE_MIN_BOUND + " 이상이어야 합니다.";
    public static final String INVALID_SEQUENCE_MAX_MESSAGE =
            "SKU 일련번호는 " + SEQUENCE_MAX_BOUND + "를 초과할 수 없습니다.";

    public static final String CATEGORY_REQUIRED_MESSAGE =
            "필수 연관 객체인 카테고리가 누락되었습니다.";
    public static final String PRODUCT_TYPE_REQUIRED_MESSAGE =
            "필수 연관 객체인 상품 타입이 누락되었습니다.";
    public static final String PRODUCT_INFO_REQUIRED_MESSAGE =
            "필수 연관 객체인 상품 상세 정보가 누락되었습니다.";
}