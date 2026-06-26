package com.kb.cosmetic_wms.member.application.port.in;

import com.kb.cosmetic_wms.member.domain.model.Role;


public record RegisterMemberCommand(
        String loginId,
        String password,
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
}
