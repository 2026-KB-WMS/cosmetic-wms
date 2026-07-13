package com.kb.ordering.auth.adapter.in.web;

import com.kb.ordering.auth.adapter.in.web.dto.LoginRequest;
import com.kb.ordering.auth.adapter.in.web.dto.LoginResponse;
import com.kb.ordering.auth.adapter.in.web.dto.SignUpRequest;
import com.kb.ordering.auth.adapter.in.web.dto.SignUpResponse;
import com.kb.ordering.auth.application.port.in.LoginUseCase;
import com.kb.ordering.auth.application.port.in.SignUpUseCase;
import com.kb.ordering.auth.application.port.in.dto.LoginResult;
import com.kb.ordering.auth.application.port.in.dto.SignUpResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(
            @Valid @RequestBody SignUpRequest request
    ) {
        SignUpResult result = signUpUseCase.signUp(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(SignUpResponse.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResult result = loginUseCase.login(request.toCommand());
        return ResponseEntity.ok(LoginResponse.from(result));
    }
}
