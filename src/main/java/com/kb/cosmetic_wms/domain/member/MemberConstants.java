package com.kb.cosmetic_wms.domain.member;

public class MemberConstants {

    private MemberConstants() {
    }

    // Login ID
    public static final int LOGIN_ID_MIN_LENGTH = 5;
    public static final int LOGIN_ID_MAX_LENGTH = 50;

    // Password
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 50;

    // Regex
    public static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    public static final String PHONE_NUMBER_REGEX =
            "^(01[016789]|02|0[3-9][0-9])-(?:\\d{3}|\\d{4})-\\d{4}$";

    public static final String PASSWORD_REGEX =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$";

    // Validation Messages
    public static final String LOGIN_ID_REQUIRED_MESSAGE =
            "로그인 ID는 필수 입력 항목입니다.";

    public static final String INVALID_LOGIN_ID_LENGTH_MESSAGE =
            "로그인 ID는 5~50자여야 합니다.";

    public static final String PASSWORD_REQUIRED_MESSAGE =
            "비밀번호는 필수 입력 항목입니다.";

    public static final String INVALID_PASSWORD_LENGTH_MESSAGE =
            "비밀번호는 8~50자여야 합니다.";

    public static final String INVALID_PASSWORD_MESSAGE =
            "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.";

    public static final String INVALID_EMAIL_MESSAGE =
            "올바르지 않은 이메일 형식입니다.";

    public static final String INVALID_PHONE_NUMBER_MESSAGE =
            "올바르지 않은 전화번호 형식입니다.";

    public static final String ROLE_REQUIRED_MESSAGE =
            "사용자 권한은 필수 선택 항목입니다.";

    public static final String MEMBER_NAME_REQUIRED_MESSAGE =
            "이름은 필수 입력값입니다.";
}
