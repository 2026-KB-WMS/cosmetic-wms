package com.kb.cosmetic_wms.domain.member.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MEMBER_NOT_FOUND",
            "존재하지 않는 회원입니다."
    ),

    DUPLICATE_LOGIN_ID(
            HttpStatus.BAD_REQUEST,
            "DUPLICATE_LOGIN_ID",
            "이미 존재하는 아이디입니다."
    ),

    INVALID_EMAIL(
            HttpStatus.BAD_REQUEST,
            "INVALID_EMAIL",
            "올바르지 않은 이메일 형식입니다."
    ),

    INVALID_PHONE_NUMBER(
            HttpStatus.BAD_REQUEST,
            "INVALID_PHONE_NUMBER",
            "올바르지 않은 전화번호 형식입니다."
    ),

    INVALID_MEMBER_VALIDATION(
            HttpStatus.BAD_REQUEST,
            "INVALID_MEMBER_VALIDATION",
            "회원 정보 유효성 검증에 실패했습니다."
    ),

    DUPLICATE_EMAIL(
            HttpStatus.BAD_REQUEST,
            "DUPLICATE_EMAIL",
            "이미 등록된 이메일 주소입니다."),

    DUPLICATE_PHONE_NUMBER(
            HttpStatus.BAD_REQUEST,
            "DUPLICATE_PHONE_NUMBER",
            "이미 등록된 전화번호입니다."
    ),

    LOGIN_FAILED(
            HttpStatus.BAD_REQUEST,
            "LOGIN_FAILED",
            "아이디 또는 비밀번호가 일치하지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
