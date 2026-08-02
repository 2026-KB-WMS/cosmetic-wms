package com.kb.auth.auth.adapter.in.web;

import com.kb.auth.auth.adapter.in.web.dto.LoginRequest;
import com.kb.auth.auth.adapter.in.web.dto.LoginResponse;
import com.kb.auth.auth.adapter.in.web.dto.LogoutRequest;
import com.kb.auth.auth.adapter.in.web.dto.ReissueRequest;
import com.kb.auth.auth.adapter.in.web.dto.ReissueResponse;
import com.kb.auth.auth.adapter.in.web.dto.SignUpRequest;
import com.kb.auth.auth.adapter.in.web.dto.SignUpResponse;
import com.kb.auth.auth.application.port.in.LoginUseCase;
import com.kb.auth.auth.application.port.in.LogoutUseCase;
import com.kb.auth.auth.application.port.in.ReissueTokenUseCase;
import com.kb.auth.auth.application.port.in.SignUpUseCase;
import com.kb.auth.auth.application.port.in.dto.LoginResult;
import com.kb.auth.auth.application.port.in.dto.ReissueResult;
import com.kb.auth.auth.application.port.in.dto.SignUpResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.noContent;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final ReissueTokenUseCase reissueTokenUseCase;
    private final LogoutUseCase logoutUseCase;

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

    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        ReissueResult result = reissueTokenUseCase.reissue(request.toCommand());
        return ResponseEntity.ok(ReissueResponse.from(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        logoutUseCase.logout(request.toCommand());
        return noContent().build();
    }
}
