package com.kb.cosmetic_wms.domain.member.dto;

import com.kb.cosmetic_wms.domain.member.MemberConstants;
import com.kb.cosmetic_wms.domain.member.enums.Role;
import jakarta.validation.constraints.*;

public record MemberSignUpRequestDto(
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
        @Pattern(
                regexp = MemberConstants.PASSWORD_REGEX,
                message = MemberConstants.INVALID_PASSWORD_MESSAGE
        )
        String password,

        @NotNull(message = MemberConstants.ROLE_REQUIRED_MESSAGE)
        Role role,

        @NotBlank(message = MemberConstants.MEMBER_NAME_REQUIRED_MESSAGE)
        String memberName,

        @Email(message = MemberConstants.INVALID_EMAIL_MESSAGE)
        String email,

        @Pattern(
                regexp = MemberConstants.PHONE_NUMBER_REGEX,
                message = MemberConstants.INVALID_PHONE_NUMBER_MESSAGE
        )
        String phoneNumber
) {
}
