package com.kb.auth.auth.application.port.in.dto;

import com.kb.auth.member.domain.model.Role;

public record LoginResult(
        Long memberId,
        String memberName,
        Role role,
        String accessToken
) {
}
