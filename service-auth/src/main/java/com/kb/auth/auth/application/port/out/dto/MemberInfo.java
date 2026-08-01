package com.kb.auth.auth.application.port.out.dto;

import com.kb.auth.member.domain.model.Role;

public record MemberInfo(
        Long memberId,
        String memberName,
        String email,
        String phoneNumber,
        Role role
) {
}
