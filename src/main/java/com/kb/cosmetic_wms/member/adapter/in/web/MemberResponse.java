package com.kb.cosmetic_wms.member.adapter.in.web;

import com.kb.cosmetic_wms.member.application.port.in.MemberResult;
import com.kb.cosmetic_wms.member.domain.model.Role;

public record MemberResponse(
        Long id,
        String loginId,
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
    public static MemberResponse from(MemberResult result) {
        return new MemberResponse(
                result.memberId(),
                result.loginId(),
                result.role(),
                result.memberName(),
                result.email(),
                result.phoneNumber()
        );
    }
}