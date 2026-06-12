package com.kb.cosmetic_wms.domain.member.exception;

public class LoginFailedException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "아이디 또는 비밀번호가 일치하지 않습니다.";

    public LoginFailedException() {
        super(DEFAULT_MESSAGE);
    }

    public LoginFailedException(String message) {
        super(message);
    }
}
