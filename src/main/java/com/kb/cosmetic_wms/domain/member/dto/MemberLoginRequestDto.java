package com.kb.cosmetic_wms.domain.member.dto;

import com.kb.cosmetic_wms.domain.member.MemberConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberLoginRequestDto(
        @NotBlank(message = MemberConstants.LOGIN_ID_REQUIRED_MESSAGE)
        @Size(
                min = MemberConstants.LOGIN_ID_MIN_LENGTH,
                max = MemberConstants.LOGIN_ID_MAX_LENGTH,
                message = MemberConstants.INVALID_LOGIN_ID_LENGTH_MESSAGE
        )
        String loginId,

        @NotBlank(message = MemberConstants.PASSWORD_REQUIRED_MESSAGE)
        @Size(
                min = MemberConstants.PASSWORD_MIN_LENGTH,
                max = MemberConstants.PASSWORD_MAX_LENGTH,
                message = MemberConstants.INVALID_PASSWORD_LENGTH_MESSAGE
        )
        String password
) {
}
