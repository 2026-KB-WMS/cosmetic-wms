package com.kb.auth.auth.application.port.in.dto;

import com.kb.auth.auth.application.port.out.dto.MemberInfo;
import com.kb.auth.member.domain.model.Role;

public record SignUpResult(
        Long memberId,
        String loginId,
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
    public static SignUpResult of(MemberInfo member, String loginId) {
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
