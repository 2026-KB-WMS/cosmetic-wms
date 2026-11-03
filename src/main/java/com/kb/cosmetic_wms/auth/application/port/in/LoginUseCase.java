package com.kb.cosmetic_wms.auth.application.port.in;

public interface LoginUseCase {

    TokenResult login(LoginCommand command);
}
