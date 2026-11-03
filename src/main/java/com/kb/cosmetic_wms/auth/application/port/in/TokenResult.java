package com.kb.cosmetic_wms.auth.application.port.in;

public record TokenResult(String accessToken, String refreshToken, long expiresInSeconds) {
}
