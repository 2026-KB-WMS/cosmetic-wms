package com.kb.auth.auth.adapter.in.web.dto;

import com.kb.auth.auth.application.port.in.dto.LogoutCommand;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "리프레시 토큰은 필수 입력 항목입니다.")
        String refreshToken
) {
    public LogoutCommand toCommand() {
        return new LogoutCommand(refreshToken);
    }
}
