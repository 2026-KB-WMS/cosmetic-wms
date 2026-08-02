package com.kb.auth.auth.application.port.in.dto;

import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.domain.model.Role;

public record SignUpResult(
        Long memberId,
        String loginId,
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
    public static SignUpResult of(MemberResult member, String loginId) {
        return new SignUpResult(
                member.memberId(),
                loginId,
                member.role(),
                member.memberName(),
                member.email(),
                member.phoneNumber()
        );
    }
}
