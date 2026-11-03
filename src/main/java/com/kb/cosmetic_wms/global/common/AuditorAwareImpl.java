package com.kb.cosmetic_wms.global.common;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {

    /**
     * 로그인 없이 일어나는 변경(회원 가입, 기동 시 부트스트랩, 비동기 이벤트 리스너 등)의 감사자.
     */
    public static final Long SYSTEM_AUDITOR_ID = 0L;

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return Optional.of(Long.valueOf(jwtAuthentication.getToken().getSubject()));
        }
        return Optional.of(SYSTEM_AUDITOR_ID);
    }
}
