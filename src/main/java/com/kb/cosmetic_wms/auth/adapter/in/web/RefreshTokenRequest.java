package com.kb.cosmetic_wms.auth.adapter.in.web;

import com.kb.cosmetic_wms.auth.application.port.in.RefreshTokenCommand;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "리프레시 토큰은 필수 입력 항목입니다.")
        String refreshToken
) {
    public RefreshTokenCommand toCommand() {
        return new RefreshTokenCommand(refreshToken);
    }
}
