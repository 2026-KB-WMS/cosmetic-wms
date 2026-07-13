package com.kb.ordering.auth.application.port.out.dto;

import com.kb.ordering.member.domain.model.Role;

public record MemberInfo(
        Long memberId,
        String memberName,
        String email,
        String phoneNumber,
        Role role
) {
}
