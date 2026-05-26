package com.kb.cosmetic_wms.domain.storage.constants;

import java.util.regex.Pattern;

public final class StorageConstants {

    private StorageConstants() {
    }

    public static final int MIN_CAPACITY_BOUND = 0;
    public static final Pattern TEMP_PATTERN = Pattern.compile("^\\d+~\\d+도$");

    public static final String WAREHOUSE_NAME_REQUIRED_MESSAGE =
            "창고 이름은 필수 입력 항목입니다.";
    public static final String ADDRESS_REQUIRED_MESSAGE =
            "창고 주소는 필수 입력 항목입니다.";
    public static final String TARGET_TEMP_REQUIRED_MESSAGE =
            "창고 적정 온도는 필수 입력 항목입니다.";
    public static final String INVALID_TARGET_TEMP_PATTERN_MESSAGE =
            "창고 적정 온도 포맷이 올바르지 않습니다. (예: 10~25도)";

    public static final String INVALID_CAPACITY_MESSAGE =
            "창고 수용 한도는 " + MIN_CAPACITY_BOUND + "보다 커야 합니다.";
}
