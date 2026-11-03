package com.kb.cosmetic_wms.auth.application.port.out;

import java.util.Optional;

public interface RefreshTokenPort {

    String issue(Long memberId);

    /**
     * 토큰을 조회하는 동시에 폐기한다. 같은 토큰으로는 한 번만 성공한다.
     */
    Optional<Long> consume(String refreshToken);

    void revoke(String refreshToken);
}
