package com.kb.cosmetic_wms.auth.adapter.in.web;

import com.kb.cosmetic_wms.auth.application.port.in.TokenResult;

public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    private static final String BEARER = "Bearer";

    public static TokenResponse from(TokenResult result) {
        return new TokenResponse(result.accessToken(), result.refreshToken(), BEARER, result.expiresInSeconds());
    }
}
