package com.kb.cosmetic_wms.domain.member.fixture;

import com.kb.cosmetic_wms.member.adapter.in.web.MemberLoginRequest;
import com.kb.cosmetic_wms.member.adapter.in.web.MemberSignUpRequest;
import com.kb.cosmetic_wms.member.domain.model.Role;
import lombok.Builder;

public final class MemberDtoBuilder {

    private MemberDtoBuilder() {
    }

    @Builder(builderMethodName = "signUpRequest", buildMethodName = "build")
    private static MemberSignUpRequest signUpRequestFactory(
            String loginId, String password, Role role,
            String memberName, String email, String phoneNumber
    ) {
        return new MemberSignUpRequest(
                loginId != null ? loginId : "admin01",
                password != null ? password : "password123!",
                role != null ? role : Role.ROLE_HEADQUARTERS,
                memberName != null ? memberName : "홍길동",
                email != null ? email : "admin@example.com",
                phoneNumber != null ? phoneNumber : "010-1234-5678"
        );
    }

    @Builder(builderMethodName = "loginRequest", buildMethodName = "build")
    public static MemberLoginRequest loginRequestFactory(
            String loginId, String password
    ) {
        return new MemberLoginRequest(
                loginId != null ? loginId : "admin01",
                password != null ? password : "password123!"
        );
    }
}