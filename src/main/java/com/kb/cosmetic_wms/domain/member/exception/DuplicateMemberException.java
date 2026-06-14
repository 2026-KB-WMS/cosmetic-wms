package com.kb.cosmetic_wms.domain.member.exception;

public class DuplicateMemberException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "이미 사용 중인 로그인 ID입니다.";

    public DuplicateMemberException() {
        super(DEFAULT_MESSAGE);
    }

    public DuplicateMemberException(String message) {
        super(message);
    }
}
