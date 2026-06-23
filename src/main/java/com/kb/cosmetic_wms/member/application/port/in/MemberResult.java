package com.kb.cosmetic_wms.member.application.port.in;

import com.kb.cosmetic_wms.member.domain.model.Member;
import com.kb.cosmetic_wms.member.domain.model.Role;

public record MemberResult(
        Long memberId,
        String loginId,
        String memberName,
        String email,
        String phoneNumber,
        Role role
) {
    public static MemberResult from(Member member) {
        return new MemberResult(
                member.getId(),
                member.getLoginId().value(),
                member.getMemberName(),
                member.getEmail().value(),
                member.getPhoneNumber().value(),
                member.getRole()
        );
    }
}