package com.kb.cosmetic_wms.domain.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.domain.member.fixture.MemberDtoBuilder;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import com.kb.cosmetic_wms.member.adapter.in.web.MemberController;
import com.kb.cosmetic_wms.member.adapter.in.web.MemberLoginRequest;
import com.kb.cosmetic_wms.member.adapter.in.web.MemberSignUpRequest;
import com.kb.cosmetic_wms.member.application.port.in.LoginMemberUseCase;
import com.kb.cosmetic_wms.member.application.port.in.MemberResult;
import com.kb.cosmetic_wms.member.application.port.in.RegisterMemberUseCase;
import com.kb.cosmetic_wms.member.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.domain.member.fixture.MemberDocumentUtils.*;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.MEMBER;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class MemberControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterMemberUseCase registerMemberUseCase;

    @MockitoBean
    private LoginMemberUseCase loginMemberUseCase;

    @Test
    @WithMockUser
    void 새로운_회원_정보를_입력하면_회원_가입에_성공하고_API_문서가_생성된다() throws Exception {
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest().build();
        MemberResult result = new MemberResult(
                1L, "admin123", "홍길동", "admin@kb.com", "010-1234-5678", Role.ROLE_HEADQUARTERS
        );

        given(registerMemberUseCase.register(any())).willReturn(result);

        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loginId").value("admin123"))
                .andDo(document("member-signup",
                        buildParams(MEMBER, "WMS 사원(회원) 등록", MEMBER_SIGNUP_REQUEST, MEMBER_DETAIL_RESPONSE),
                        getSignupRequestFields(),
                        getMemberDetailResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 회원가입_실패_아이디가_공백이면_400_BAD_REQUEST를_반환한다() throws Exception {
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest()
                .loginId(" ")
                .build();

        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-signup-fail-blank-id",
                        buildErrorParams(MEMBER, "회원 가입 실패 - 아이디 공백")
                ));
    }

    @ParameterizedTest
    @WithMockUser
    @ValueSource(strings = {"1234", "  ", "가나다"})
    void 회원가입_실패_비밀번호_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다(String invalidPwd) throws Exception {
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest()
                .password(invalidPwd)
                .build();

        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-signup-fail-invalid-password/{index}",
                        buildErrorParams(MEMBER, "회원 가입 실패 - 비밀번호 형식 오류")
                ));
    }

    @Test
    @WithMockUser
    void 회원가입_실패_회원명이_공백이면_400_BAD_REQUEST를_반환한다() throws Exception {
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest()
                .memberName(" ")
                .build();

        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-signup-fail-blank-memberName",
                        buildErrorParams(MEMBER, "회원 가입 실패 - 회원명 공백")
                ));
    }

    @ParameterizedTest
    @WithMockUser
    @ValueSource(strings = {"adsfr", " ", "adsfwe@"})
    void 회원가입_실패_이메일_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다(String invalidEmail) throws Exception {
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest()
                .email(invalidEmail)
                .build();

        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-signup-fail-invalid-email/{index}",
                        buildErrorParams(MEMBER, "회원 가입 실패 - 이메일 형식 오류")
                ));
    }

    @Test
    @WithMockUser
    void 회원가입_실패_전화번호_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다() throws Exception {
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest()
                .phoneNumber("01012345678")
                .build();

        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-signup-fail-invalid-phoneNumber",
                        buildErrorParams(MEMBER, "회원 가입 실패 - 전화번호 형식 오류")
                ));
    }

    @Test
    @WithMockUser
    void 올바른_로그인_아이디와_비밀번호를_입력하면_200_OK와_함께_회원_정보를_반환하고_API_문서가_생성된다() throws Exception {
        MemberLoginRequest request = MemberDtoBuilder.loginRequest().build();
        MemberResult result = new MemberResult(
                1L, "admin01", "홍길동", "admin@example.com", "010-1234-5678", Role.ROLE_HEADQUARTERS
        );

        given(loginMemberUseCase.login(any())).willReturn(result);

        mockMvc.perform(post("/api/v1/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loginId").value("admin01"))
                .andExpect(jsonPath("$.role").value("ROLE_HEADQUARTERS"))
                .andDo(document("member-login-success",
                        buildParams(MEMBER, "WMS 사원 로그인", MEMBER_LOGIN_REQUEST, MEMBER_DETAIL_RESPONSE),
                        getLoginRequestFields(),
                        getMemberDetailResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 비밀번호가_공백이면_400_BAD_REQUEST를_반환한다() throws Exception {
        MemberLoginRequest request = MemberDtoBuilder.loginRequest()
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-login-fail-blank-password",
                        buildErrorParams(MEMBER, "로그인 실패 - 비밀번호 공백")
                ));
    }
}