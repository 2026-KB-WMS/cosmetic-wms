package com.kb.auth.member.application.port.in.dto;

import com.kb.auth.member.domain.model.Member;
import com.kb.auth.member.domain.model.Role;

public record MemberResult(
        Long memberId,
        String memberName,
        String email,
        String phoneNumber,
        Role role
) {
    public static MemberResult from(Member member) {
        return new MemberResult(
                member.getMemberId(),
                member.getMemberName(),
                member.getEmail().value(),
                member.getPhoneNumber().value(),
                member.getRole()
        );
    }
}
