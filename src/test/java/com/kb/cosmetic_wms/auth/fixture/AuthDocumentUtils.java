package com.kb.cosmetic_wms.auth.fixture;

import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.key;

public final class AuthDocumentUtils {

    private AuthDocumentUtils() {
    }

    public static RequestFieldsSnippet getLoginRequestFields() {
        return requestFields(
                fieldWithPath("loginId").description("로그인 아이디")
                        .attributes(key("example").value("admin01")),
                fieldWithPath("password").description("비밀번호")
                        .attributes(key("example").value("password123!"))
        );
    }

    public static RequestFieldsSnippet getRefreshTokenRequestFields() {
        return requestFields(
                fieldWithPath("refreshToken").description("로그인 또는 재발급 응답으로 받은 리프레시 토큰")
                        .attributes(key("example").value("q3J0b2tlbi1leGFtcGxl"))
        );
    }

    public static ResponseFieldsSnippet getTokenResponseFields() {
        return responseFields(
                fieldWithPath("accessToken").description("API 호출에 쓰는 액세스 토큰. `Authorization: Bearer <토큰>` 헤더로 전달한다")
                        .attributes(key("example").value("eyJhbGciOiJIUzI1NiJ9...")),
                fieldWithPath("refreshToken").description("액세스 토큰 재발급에 쓰는 리프레시 토큰. 재발급할 때마다 새로 발급되고 이전 토큰은 폐기된다")
                        .attributes(key("example").value("q3J0b2tlbi1leGFtcGxl")),
                fieldWithPath("tokenType").description("토큰 유형")
                        .attributes(key("example").value("Bearer")),
                fieldWithPath("expiresIn").description("액세스 토큰 만료까지 남은 시간(초)")
                        .attributes(key("example").value(1800))
        );
    }
}
