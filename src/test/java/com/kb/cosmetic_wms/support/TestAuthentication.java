package com.kb.cosmetic_wms.support;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

/**
 * 서비스를 직접 호출하는 테스트에서 특정 회원이 로그인한 상태를 흉내 낸다.
 */
public final class TestAuthentication {

    private TestAuthentication() {
    }

    public static void authenticateAs(long memberId) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(String.valueOf(memberId))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
