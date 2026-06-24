package com.kb.cosmetic_wms.product.domain.constants;

public final class ProductConstants {

    private ProductConstants() {
    }

    public static final String SKU_FORMAT = "P%06d";
    public static final int MIN_PRICE_BOUND = 0;

    public static final int BRAND_NAME_MAX_LENGTH = 100;
    public static final int PRODUCT_NAME_MAX_LENGTH = 255;

    public static final String BRAND_NAME_REQUIRED_MESSAGE = "브랜드명은 필수 입력 항목입니다.";
    public static final String INVALID_BRAND_NAME_LENGTH_MESSAGE =
            "브랜드명은 " + BRAND_NAME_MAX_LENGTH + "자를 초과할 수 없습니다.";
    public static final String PRODUCT_NAME_REQUIRED_MESSAGE = "상품명은 필수 입력 항목입니다.";
    public static final String INVALID_PRODUCT_NAME_LENGTH_MESSAGE =
            "상품명은 " + PRODUCT_NAME_MAX_LENGTH + "자를 초과할 수 없습니다.";
    public static final String INVALID_PRODUCT_PRICE_MESSAGE =
            "상품 가격은 " + MIN_PRICE_BOUND + "원 이상이어야 합니다.";
    public static final String TEMPERATURE_TYPE_REQUIRED_MESSAGE = "보관 온도 타입은 필수 입력 항목입니다.";
    public static final String CATEGORY_ID_REQUIRED_MESSAGE = "카테고리 ID는 필수 입력 항목입니다.";
    public static final String PRODUCT_TYPE_ID_REQUIRED_MESSAGE = "상품 타입 ID는 필수 입력 항목입니다.";
    public static final String PRODUCT_INFO_REQUEST_REQUIRED_MESSAGE = "상품 상세 정보는 필수 입력 항목입니다.";

    public static final String CATEGORY_REQUIRED_MESSAGE = "필수 연관 객체인 카테고리가 누락되었습니다.";
    public static final String PRODUCT_TYPE_REQUIRED_MESSAGE = "필수 연관 객체인 상품 타입이 누락되었습니다.";
    public static final String PRODUCT_INFO_REQUIRED_MESSAGE = "필수 연관 객체인 상품 상세 정보가 누락되었습니다.";
}