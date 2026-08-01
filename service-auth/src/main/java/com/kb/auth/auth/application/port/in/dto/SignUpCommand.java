package com.kb.auth.auth.application.port.in.dto;

import com.kb.auth.member.domain.model.Role;

public record SignUpCommand(
        String loginId,
        String password,
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
}
