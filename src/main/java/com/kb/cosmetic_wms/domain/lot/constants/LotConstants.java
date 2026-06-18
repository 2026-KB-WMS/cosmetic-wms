package com.kb.cosmetic_wms.domain.lot.constants;

import java.util.regex.Pattern;

public final class LotConstants {

    private LotConstants() {
    }

    public static final int LOT_NUMBER_MAX_LENGTH = 50;

    // [상품 카테고리 3자리]-[제조년월일 6자리]-[공장코드 2자리]-[일련번호 4자리]
    public static final String LOT_NO_PATTERN_REGEX = "^[A-Z]{3}-\\d{6}-[A-Z0-9]{2}-\\d{4}$";
    public static final Pattern LOT_NO_PATTERN = Pattern.compile(LOT_NO_PATTERN_REGEX);

    public static final String MANUFACTURING_DATE_REQUIRED_MESSAGE =
            "제조일자는 필수 입력 값입니다.";

    public static final String EXPIRATION_DATE_REQUIRED_MESSAGE =
            "유통기한은 필수 입력 값입니다.";

    public static final String DATES_REQUIRED_MESSAGE =
            "제조일자와 유통기한은 필수 입력 값입니다.";

    public static final String INVALID_MANUFACTURE_DATE_MESSAGE =
            "제조일자는 유통기한보다 미래일 수 없습니다.";

    public static final String LOT_NO_REQUIRED_MESSAGE =
            "로트 번호는 필수 입력 값입니다.";

    public static final String INVALID_LOT_NUMBER_LENGTH_MESSAGE =
            "로트 번호는 최대 " + LOT_NUMBER_MAX_LENGTH + "자까지 입력 가능합니다.";

    public static final String INVALID_LOT_NO_FORMAT_MESSAGE =
            "올바르지 않은 로트 번호 형식입니다. (규격: [카테고리3자]-[YYMMDD]-[공장2자]-[일련번호4자])";

    public static final String PRODUCT_REQUIRED_MESSAGE =
            "상품 식별자(ID)는 필수입니다.";
}
