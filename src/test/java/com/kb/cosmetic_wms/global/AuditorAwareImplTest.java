package com.kb.cosmetic_wms.global;

import com.kb.cosmetic_wms.global.common.AuditorAwareImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditorAwareImplTest {

    private final AuditorAwareImpl auditorAware = new AuditorAwareImpl();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 로그인한_사용자가_있으면_토큰의_회원_ID를_감사자로_사용한다() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("42")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

        assertThat(auditorAware.getCurrentAuditor()).contains(42L);
    }

    @Test
    void 인증_정보가_없으면_시스템_감사자를_사용한다() {
        assertThat(auditorAware.getCurrentAuditor()).contains(AuditorAwareImpl.SYSTEM_AUDITOR_ID);
    }

    @Test
    void JWT가_아닌_인증이면_시스템_감사자를_사용한다() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "pw", List.of()));

        assertThat(auditorAware.getCurrentAuditor()).contains(AuditorAwareImpl.SYSTEM_AUDITOR_ID);
    }
}
