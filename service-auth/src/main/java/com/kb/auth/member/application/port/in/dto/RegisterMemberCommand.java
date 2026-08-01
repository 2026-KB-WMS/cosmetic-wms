package com.kb.auth.member.application.port.in.dto;

import com.kb.auth.member.domain.model.Role;

public record RegisterMemberCommand(
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
}
