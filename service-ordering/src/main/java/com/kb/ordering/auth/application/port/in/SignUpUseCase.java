package com.kb.ordering.auth.application.port.in;

import com.kb.ordering.auth.application.port.in.dto.SignUpCommand;
import com.kb.ordering.auth.application.port.in.dto.SignUpResult;

public interface SignUpUseCase {

    SignUpResult signUp(SignUpCommand command);
}
