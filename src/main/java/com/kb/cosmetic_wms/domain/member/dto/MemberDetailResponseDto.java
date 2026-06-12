package com.kb.cosmetic_wms.domain.member.dto;

import com.kb.cosmetic_wms.domain.member.entity.Member;

public record MemberDetailResponseDto(
        Long id,
        String name
) {
    public static MemberDetailResponseDto from(Member member) {
        return new MemberDetailResponseDto(member.getId(), member.getMemberName());
    }
}
