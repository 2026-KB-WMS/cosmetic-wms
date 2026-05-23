package com.kb.cosmetic_wms.domain.product.productInfo;

public class ProductInfoConstants {

    private ProductInfoConstants() {
    }

    // 용량 제한 경계값
    public static final int MIN_VOLUME = 0;
    public static final int MAX_VOLUME = 10000;

    // Validation Messages
    public static final String INVALID_VOLUME_MIN_MESSAGE = "화장품 용량은 0보다 커야 합니다.";
    public static final String INVALID_VOLUME_MAX_MESSAGE = "올바르지 않은 대용량 수치입니다. (최대 10,000까지 허용)";

}
