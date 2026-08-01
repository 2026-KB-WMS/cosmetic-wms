package com.kb.auth.auth.adapter.in.web.dto;

import com.kb.auth.auth.application.port.in.dto.SignUpResult;
import com.kb.auth.member.domain.model.Role;

public record SignUpResponse(
        Long memberId,
        String loginId,
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
    public static SignUpResponse from(SignUpResult result) {
        return new SignUpResponse(
                result.memberId(),
                result.loginId(),
                result.role(),
                result.memberName(),
                result.email(),
                result.phoneNumber()
        );
    }
}
