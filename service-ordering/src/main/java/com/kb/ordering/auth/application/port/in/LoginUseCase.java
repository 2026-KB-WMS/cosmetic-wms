package com.kb.ordering.auth.application.port.in;

import com.kb.ordering.auth.application.port.in.dto.LoginCommand;
import com.kb.ordering.auth.application.port.in.dto.LoginResult;

public interface LoginUseCase {

    LoginResult login(LoginCommand command);
}
