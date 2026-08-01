package com.kb.auth.auth.adapter.in.web.dto;

import com.kb.auth.auth.application.port.in.dto.LoginResult;
import com.kb.auth.member.domain.model.Role;

public record LoginResponse(
        Long memberId,
        String memberName,
        Role role,
        String accessToken
) {
    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.memberId(),
                result.memberName(),
                result.role(),
                result.accessToken()
        );
    }
}
