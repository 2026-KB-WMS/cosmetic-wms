package com.kb.cosmetic_wms.domain.member.dto;

import com.kb.cosmetic_wms.domain.member.entity.Member;
import com.kb.cosmetic_wms.domain.member.enums.Role;

public record MemberDetailResponseDto(
        Long id,
        String loginId,
        Role role,
        String memberName,
        String email,
        String phoneNumber
) {
    public static MemberDetailResponseDto from(Member member) {
        return new MemberDetailResponseDto(
                member.getId(),
                member.getLoginId(),
                member.getRole(),
                member.getMemberName(),
                member.getEmail(),
                member.getPhoneNumber()
        );
    }
}
