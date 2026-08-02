package com.kb.auth.member.adapter.in.web.dto;

import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.domain.model.Role;

public record MemberResponse(
        Long memberId,
        String memberName,
        String email,
        String phoneNumber,
        Role role
) {
    public static MemberResponse from(MemberResult result) {
        return new MemberResponse(
                result.memberId(),
                result.memberName(),
                result.email(),
                result.phoneNumber(),
                result.role()
        );
    }
}
