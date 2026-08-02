package com.kb.auth.auth.application.port.in;

import com.kb.auth.auth.application.port.in.dto.LogoutCommand;

public interface LogoutUseCase {

    void logout(LogoutCommand command);
}
