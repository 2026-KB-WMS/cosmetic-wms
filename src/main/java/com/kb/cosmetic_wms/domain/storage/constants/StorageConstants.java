package com.kb.cosmetic_wms.domain.storage.constants;

import java.util.regex.Pattern;

public final class StorageConstants {

    private StorageConstants() {
    }

    public static final int MIN_CAPACITY_BOUND = 0;

    public static final Pattern TEMP_PATTERN = Pattern.compile("^\\d+~\\d+도$");

    // [창고ID 4자리]-[타입 4자리]-[온도 1자리]-[순번 2자리]
    public static final Pattern SECTION_CODE_PATTERN =
            Pattern.compile("^WH\\d{2}-(DOCK|HIGH|MID|LOW|QUAR)-[RC]-\\d{2}$");

    // Warehouse: Validation Messages
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

    public static final String EXCEED_WAREHOUSE_CAPACITY_MESSAGE =
            "하위 섹션들의 최대 수용량 합이 창고 전체 수용 한도를 초과할 수 없습니다.";

    public static final String DUPLICATE_SECTION_CODE_MESSAGE =
            "해당 창고에 이미 동일한 섹션이 존재합니다.";

    // Section: Validation Messages
    public static final String INVALID_SECTION_MAX_CAPACITY_MESSAGE =
            "섹션의 최대 수용 가능 수량은 " + MIN_CAPACITY_BOUND + "보다 커야 합니다.";

    public static final String SECTION_CAPACITY_OVERFLOW_MESSAGE =
            "섹션의 최대 수용 가능 수량을 초과할 수 없습니다.";

    public static final String SECTION_CAPACITY_UNDERFLOW_MESSAGE =
            "섹션의 현재 수량이 " + MIN_CAPACITY_BOUND + "보다 작아질 수 없습니다.";

    public static final String SECTION_CODE_REQUIRED_MESSAGE =
            "섹션 코드는 필수 입력 항목입니다.";

    public static final String INVALID_SECTION_CODE_PATTERN_MESSAGE =
            "섹션 코드 포맷이 올바르지 않습니다. (예: WH01-HIGH-R-01)";

    public static final String SECTION_NAME_REQUIRED_MESSAGE =
            "섹션 이름은 필수 입력 항목입니다.";

    public static final String INVALID_SECTION_STATE_COMBINATION_MESSAGE =
            "구역 타입에 올바르지 않은 품질 상태 또는 할당 상태 조합입니다.";

    public static final String QUARANTINE_MUST_BE_ROOM_MESSAGE =
            "격리 구역의 온도 타입은 COOL(냉장)일 수 없습니다. 상온(ROOM)으로 설정해주세요.";

    // Section Code Generator
    public static final String INVALID_SECTION_SEQUENCE_MESSAGE =
            "섹션 발행 순번은 1 이상이어야 합니다.";
}
