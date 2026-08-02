package com.kb.auth.auth.adapter.in.web.dto;

import com.kb.auth.auth.application.port.in.dto.ReissueCommand;
import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
        @NotBlank(message = "리프레시 토큰은 필수 입력 항목입니다.")
        String refreshToken
) {
    public ReissueCommand toCommand() {
        return new ReissueCommand(refreshToken);
    }
}
