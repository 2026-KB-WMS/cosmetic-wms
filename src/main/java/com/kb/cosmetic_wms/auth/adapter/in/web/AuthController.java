package com.kb.cosmetic_wms.auth.adapter.in.web;

import com.kb.cosmetic_wms.auth.application.port.in.LoginUseCase;
import com.kb.cosmetic_wms.auth.application.port.in.LogoutUseCase;
import com.kb.cosmetic_wms.auth.application.port.in.ReissueTokenUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final ReissueTokenUseCase reissueTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(TokenResponse.from(loginUseCase.login(request.toCommand())));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(TokenResponse.from(reissueTokenUseCase.reissue(request.toCommand())));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logoutUseCase.logout(request.toCommand());
        return ResponseEntity.noContent().build();
    }
}
