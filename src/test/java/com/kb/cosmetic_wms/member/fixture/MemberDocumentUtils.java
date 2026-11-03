package com.kb.cosmetic_wms.member.fixture;

import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;

public final class MemberDocumentUtils {
    private MemberDocumentUtils() {
    }

    /**
     * 회원가입 요청 필드 명세
     */
    public static RequestFieldsSnippet getSignupRequestFields() {
        return requestFields(
                fieldWithPath("loginId").description("로그인 아이디")
                        .attributes(key("example").value("admin01")),
                fieldWithPath("password").description("비밀번호")
                        .attributes(key("example").value("password123!")),
                fieldWithPath("role").description("회원 권한 (ROLE_WAREHOUSE_MANAGER, ROLE_FRANCHISE_MANAGER). ROLE_HEADQUARTERS는 직접 가입할 수 없다")
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
     * 회원 상세 정보 응답 필드 명세 (가입/로그인 공통 재사용)
     */
    public static ResponseFieldsSnippet getMemberDetailResponseFields() {
        return responseFields(
                fieldWithPath("id").description("생성된 고유 회원 ID (PK)")
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
}
