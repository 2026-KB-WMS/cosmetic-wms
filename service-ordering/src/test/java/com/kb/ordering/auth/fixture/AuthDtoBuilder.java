package com.kb.ordering.auth.fixture;

import com.kb.ordering.auth.adapter.in.web.dto.LoginRequest;
import com.kb.ordering.auth.adapter.in.web.dto.SignUpRequest;
import com.kb.ordering.member.domain.model.Role;
import lombok.Builder;

public final class AuthDtoBuilder {

    private AuthDtoBuilder() {
    }

    @Builder(builderMethodName = "signUpRequest", buildMethodName = "build")
    private static SignUpRequest signUpRequestFactory(
            String loginId, String password, Role role,
            String memberName, String email, String phoneNumber
    ) {
        return new SignUpRequest(
                loginId != null ? loginId : "admin01",
                password != null ? password : "password123!",
                role != null ? role : Role.ROLE_HEADQUARTERS,
                memberName != null ? memberName : "홍길동",
                email != null ? email : "admin@example.com",
                phoneNumber != null ? phoneNumber : "010-1234-5678"
        );
    }

    @Builder(builderMethodName = "loginRequest", buildMethodName = "build")
    private static LoginRequest loginRequestFactory(
            String loginId, String password
    ) {
        return new LoginRequest(
                loginId != null ? loginId : "admin01",
                password != null ? password : "password123!"
        );
    }
}
