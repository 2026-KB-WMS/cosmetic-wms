package com.kb.auth.auth.application.port.out;

import java.util.Optional;

public interface RefreshTokenPort {

    void save(Long memberId, String refreshToken);

    Optional<String> find(Long memberId);

    void delete(Long memberId);
}
