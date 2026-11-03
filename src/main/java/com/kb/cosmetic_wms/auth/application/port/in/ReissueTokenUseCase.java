package com.kb.cosmetic_wms.auth.application.port.in;

public interface ReissueTokenUseCase {

    TokenResult reissue(RefreshTokenCommand command);
}
