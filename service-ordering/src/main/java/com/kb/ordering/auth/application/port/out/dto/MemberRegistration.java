package com.kb.ordering.auth.application.port.out.dto;

import com.kb.ordering.member.domain.model.Role;

public record MemberRegistration(
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
}
