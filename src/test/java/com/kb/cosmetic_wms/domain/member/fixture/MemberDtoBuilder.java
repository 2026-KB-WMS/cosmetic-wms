package com.kb.cosmetic_wms.domain.member.fixture;

import com.kb.cosmetic_wms.domain.member.dto.MemberLoginRequestDto;
import com.kb.cosmetic_wms.domain.member.dto.MemberSignUpRequestDto;
import com.kb.cosmetic_wms.domain.member.enums.Role;
import lombok.Builder;

public final class MemberDtoBuilder {

    private MemberDtoBuilder() {
    }

    /**
     * 회원가입 요청 DTO
     */
    @Builder(builderMethodName = "signUpRequest", buildMethodName = "build")
    private static MemberSignUpRequestDto signUpRequestFactory(
            String loginId, String password, Role role,
            String memberName, String email, String phoneNumber
    ) {
        return new MemberSignUpRequestDto(
                loginId != null ? loginId : "admin01",
                password != null ? password : "password123!",
                role != null ? role : Role.ROLE_HEADQUARTERS,
                memberName != null ? memberName : "홍길동",
                email != null ? email : "admin@example.com",
                phoneNumber != null ? phoneNumber : "010-1234-5678"
        );
    }

    /**
     * 로그인 요청 DTO
     */
    @Builder(builderMethodName = "loginRequest", buildMethodName = "build")
    public static MemberLoginRequestDto LoginRequestFactory(
            String loginId, String password
    ) {
        return new MemberLoginRequestDto(
                loginId != null ? loginId : "admin01",
                password != null ? password : "password123!"
        );
    }
}
