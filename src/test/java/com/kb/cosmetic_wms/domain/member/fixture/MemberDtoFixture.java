package com.kb.cosmetic_wms.domain.member.fixture;

import com.kb.cosmetic_wms.domain.member.dto.MemberSignUpRequestDto;
import com.kb.cosmetic_wms.domain.member.enums.Role;

public final class MemberDtoFixture {

    private MemberDtoFixture() {
    }

    /**
     * 회원가입 요청 DTO
     */
    public static MemberSignUpRequestDto createSignUpRequest() {
        return createSignUpRequest("admin01", Role.ROLE_HEADQUARTERS);
    }

    public static MemberSignUpRequestDto createSignUpRequest(String loginId, Role role) {
        return new MemberSignUpRequestDto(
                loginId,
                "password123!",
                role,
                "홍길동",
                "test@cosmetic.com",
                "010-1234-5678"
        );
    }
}
