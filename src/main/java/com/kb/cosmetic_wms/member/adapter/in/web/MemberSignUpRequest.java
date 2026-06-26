package com.kb.cosmetic_wms.member.adapter.in.web;

import com.kb.cosmetic_wms.member.application.port.in.RegisterMemberCommand;
import com.kb.cosmetic_wms.member.domain.model.Role;
import com.kb.cosmetic_wms.member.domain.valueobject.LoginId;
import jakarta.validation.constraints.*;

public record MemberSignUpRequest(
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
    public RegisterMemberCommand toCommand() {
        return new RegisterMemberCommand(loginId, password, role, memberName, email, phoneNumber);
    }
}