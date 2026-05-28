package com.kb.cosmetic_wms.domain.inventory.constants;

import java.util.regex.Pattern;

public final class LotConstants {

    private LotConstants() {
    }

    // [상품 카테고리 3자리]-[제조년월일 6자리]-[공장코드 2자리]-[일련번호 4자리]
    public static final Pattern LOT_NO_PATTERN =
            Pattern.compile("^[A-Z]{3}-\\d{6}-[A-Z0-9]{2}-\\d{4}$");
}
