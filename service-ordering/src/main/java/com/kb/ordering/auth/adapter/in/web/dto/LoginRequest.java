package com.kb.ordering.auth.adapter.in.web.dto;

import com.kb.ordering.auth.application.port.in.dto.LoginCommand;
import com.kb.ordering.auth.domain.valueobject.LoginId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "로그인 ID는 필수 입력 항목입니다.")
        @Size(
                min = LoginId.MIN_LENGTH,
                max = LoginId.MAX_LENGTH,
                message = "로그인 ID는 5~50자여야 합니다."
        )
        String loginId,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8~50자여야 합니다.")
        String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(loginId, password);
    }
}
