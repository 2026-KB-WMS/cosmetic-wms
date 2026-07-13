package com.kb.ordering.auth.adapter.in.web.dto;

import com.kb.ordering.auth.application.port.in.dto.SignUpCommand;
import com.kb.ordering.auth.domain.valueobject.LoginId;
import com.kb.ordering.member.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "로그인 ID는 필수 입력 항목입니다.")
        @Size(
                min = LoginId.MIN_LENGTH,
                max = LoginId.MAX_LENGTH,
                message = "로그인 ID는 5~50자여야 합니다."
        )
        String loginId,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8~50자여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
        )
        String password,

        @NotNull(message = "사용자 권한은 필수 선택 항목입니다.")
        Role role,

        @NotBlank(message = "이름은 필수 입력값입니다.")
        String memberName,

        @Email(message = "올바르지 않은 이메일 형식입니다.")
        String email,

        @Pattern(
                regexp = "^(01[016789]|02|0[3-9][0-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
                message = "올바르지 않은 전화번호 형식입니다."
        )
        String phoneNumber
) {
    public SignUpCommand toCommand() {
        return new SignUpCommand(loginId, password, role, memberName, email, phoneNumber);
    }
}
