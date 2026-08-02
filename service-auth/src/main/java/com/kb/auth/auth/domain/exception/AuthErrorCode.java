package com.kb.auth.auth.domain.exception;

import com.kb.common.error.ErrorCode;
import com.kb.common.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    LOGIN_FAILED(
            ErrorType.INVALID_REQUEST,
            "LOGIN_FAILED",
            "아이디 또는 비밀번호가 일치하지 않습니다."
    ),

    DUPLICATE_LOGIN_ID(
            ErrorType.INVALID_REQUEST,
            "DUPLICATE_LOGIN_ID",
            "이미 존재하는 아이디입니다."
    ),

    INVALID_CREDENTIAL(
            ErrorType.INVALID_REQUEST,
            "INVALID_CREDENTIAL",
            "자격증명 유효성 검증에 실패했습니다."
    ),

    INVALID_REFRESH_TOKEN(
            ErrorType.UNAUTHORIZED,
            "INVALID_REFRESH_TOKEN",
            "유효하지 않거나 만료된 리프레시 토큰입니다."
    ),

    HEADQUARTERS_ROLE_NOT_ALLOWED(
            ErrorType.INVALID_REQUEST,
            "HEADQUARTERS_ROLE_NOT_ALLOWED",
            "본사 관리자 계정은 직접 생성할 수 없습니다."
    );

    private final ErrorType type;
    private final String code;
    private final String message;
}
