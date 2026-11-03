package com.kb.cosmetic_wms.auth.adapter.in.web;

import com.kb.cosmetic_wms.auth.application.port.in.LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "로그인 ID는 필수 입력 항목입니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(loginId, password);
    }
}
