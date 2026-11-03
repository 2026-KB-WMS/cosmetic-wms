package com.kb.cosmetic_wms.auth.application.port.in;

public interface LogoutUseCase {

    void logout(RefreshTokenCommand command);
}
