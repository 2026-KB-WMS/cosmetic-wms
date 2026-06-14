package com.kb.cosmetic_wms.global.error.dto;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponseDto of(String errorCode, String message) {
        return new ErrorResponseDto(errorCode, message, LocalDateTime.now());
    }
}
