package com.kb.auth.auth.application.port.in.dto;

public record ReissueResult(
        Long memberId,
        String accessToken,
        String refreshToken
) {
}