package com.kb.ordering.member.application.port.in.dto;

import com.kb.ordering.member.domain.model.Role;

public record RegisterMemberCommand(
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
}
