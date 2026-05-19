package com.kb.cosmetic_wms.domain.member;

public class MemberConstants {

    private MemberConstants() {
    }

    public static final int LOGIN_ID_MIN_LENGTH = 5;
    public static final int LOGIN_ID_MAX_LENGTH = 50;

    public static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    public static final String PHONE_NUMBER_REGEX =
            "^(01[016789]|02|0[3-9][0-9])-(?:\\d{3}|\\d{4})-\\d{4}$";
}
