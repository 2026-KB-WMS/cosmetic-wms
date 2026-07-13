package com.kb.ordering.auth.fixture;

import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;

public final class AuthDocumentUtils {
    private AuthDocumentUtils() {
    }

    /**
     * 회원가입 요청 필드 명세
     */
    public static RequestFieldsSnippet getSignUpRequestFields() {
        return requestFields(
                fieldWithPath("loginId").description("로그인 아이디")
                        .attributes(key("example").value("admin01")),
                fieldWithPath("password").description("비밀번호")
                        .attributes(key("example").value("password123!")),
                fieldWithPath("role").description("회원 권한 (ROLE_HEADQUARTERS, ROLE_WAREHOUSE_MANAGER, ROLE_FRANCHISE_MANAGER)")
                        .attributes(key("example").value("ROLE_WAREHOUSE_MANAGER")),
                fieldWithPath("memberName").description("이름(성명)")
                        .attributes(key("example").value("홍길동")),
                fieldWithPath("email").description("이메일 주소")
                        .attributes(key("example").value("admin@example.com")),
                fieldWithPath("phoneNumber").description("전화번호")
                        .attributes(key("example").value("010-1234-5678"))
        );
    }

    /**
     * 로그인 요청 필드 명세
     */
    public static RequestFieldsSnippet getLoginRequestFields() {
        return requestFields(
                fieldWithPath("loginId").description("로그인 아이디")
                        .attributes(key("example").value("admin01")),
                fieldWithPath("password").description("비밀번호")
                        .attributes(key("example").value("password123!"))
        );
    }

    /**
     * 회원가입 응답 필드 명세
     */
    public static ResponseFieldsSnippet getSignUpResponseFields() {
        return responseFields(
                fieldWithPath("memberId").description("생성된 고유 회원 ID (PK)")
                        .attributes(key("example").value(1)),
                fieldWithPath("loginId").description("로그인 아이디")
                        .attributes(key("example").value("admin01")),
                fieldWithPath("role").description("회원 권한")
                        .attributes(key("example").value("ROLE_HEADQUARTERS")),
                fieldWithPath("memberName").description("이름(성명)")
                        .attributes(key("example").value("홍길동")),
                fieldWithPath("email").description("이메일 주소")
                        .attributes(key("example").value("admin@example.com")),
                fieldWithPath("phoneNumber").description("전화번호")
                        .attributes(key("example").value("010-1234-5678"))
        );
    }

    /**
     * 로그인 응답 필드 명세
     */
    public static ResponseFieldsSnippet getLoginResponseFields() {
        return responseFields(
                fieldWithPath("memberId").description("회원 ID (PK)")
                        .attributes(key("example").value(1)),
                fieldWithPath("memberName").description("이름(성명)")
                        .attributes(key("example").value("홍길동")),
                fieldWithPath("role").description("회원 권한")
                        .attributes(key("example").value("ROLE_HEADQUARTERS")),
                fieldWithPath("accessToken").description("발급된 액세스 토큰")
                        .attributes(key("example").value("stub-token.1.ROLE_HEADQUARTERS"))
        );
    }
}
