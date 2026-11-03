package com.kb.cosmetic_wms.auth.application.port.out;

public record IssuedAccessToken(String token, long expiresInSeconds) {
}
