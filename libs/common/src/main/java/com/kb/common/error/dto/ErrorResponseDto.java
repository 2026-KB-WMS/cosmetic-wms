package com.kb.common.error.dto;

import com.kb.common.error.ErrorCode;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponseDto of(ErrorCode errorCode) {
        return new ErrorResponseDto(
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now()
        );
    }

    public static ErrorResponseDto ofValidation(String message) {
        return new ErrorResponseDto("INVALID_INPUT", message, LocalDateTime.now());
    }
}