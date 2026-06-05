package com.kb.cosmetic_wms.global.common;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {

    /**
     * 로그인 기능 구현 전까지 임시로 구현
     */
    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.of(1L);
    }
}
