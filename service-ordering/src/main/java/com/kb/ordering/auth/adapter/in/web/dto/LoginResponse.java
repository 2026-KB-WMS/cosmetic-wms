package com.kb.ordering.auth.adapter.in.web.dto;

import com.kb.ordering.auth.application.port.in.dto.LoginResult;
import com.kb.ordering.member.domain.model.Role;

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
