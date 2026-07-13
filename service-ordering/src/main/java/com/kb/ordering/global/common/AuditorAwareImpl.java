package com.kb.ordering.global.common;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {

    /**
     * 임시 구현: JWT 인증 도입 시 SecurityContext에서 사용자 ID를 조회하도록 교체 필요.
     */
    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.of(1L);
    }
}
