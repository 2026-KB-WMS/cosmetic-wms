package com.kb.cosmetic_wms.domain.member.dto;

import com.kb.cosmetic_wms.domain.member.enums.Role;

public record MemberSignUpRequest(
        String loginId,
        String password,
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
}
