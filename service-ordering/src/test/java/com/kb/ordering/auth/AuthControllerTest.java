package com.kb.ordering.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.common.error.GlobalExceptionHandler;
import com.kb.ordering.auth.adapter.in.web.AuthController;
import com.kb.ordering.auth.adapter.in.web.dto.LoginRequest;
import com.kb.ordering.auth.adapter.in.web.dto.SignUpRequest;
import com.kb.ordering.auth.application.port.in.LoginUseCase;
import com.kb.ordering.auth.application.port.in.SignUpUseCase;
import com.kb.ordering.auth.application.port.in.dto.LoginResult;
import com.kb.ordering.auth.application.port.in.dto.SignUpResult;
import com.kb.ordering.auth.fixture.AuthDtoBuilder;
import com.kb.ordering.global.restdocs.RestDocsSupport;
import com.kb.ordering.member.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.ordering.auth.fixture.AuthDocumentUtils.*;
import static com.kb.ordering.global.restdocs.ApiDocs.AUTH;
import static com.kb.ordering.global.restdocs.ApiSchemas.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SignUpUseCase signUpUseCase;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @Test
    void 새로운_회원_정보를_입력하면_회원_가입에_성공하고_API_문서가_생성된다() throws Exception {
        SignUpRequest request = AuthDtoBuilder.signUpRequest().build();
        SignUpResult result = new SignUpResult(
                1L, "admin01", Role.ROLE_HEADQUARTERS, "홍길동", "admin@example.com", "010-1234-5678"
        );

        given(signUpUseCase.signUp(any())).willReturn(result);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(1L))
                .andExpect(jsonPath("$.loginId").value("admin01"))
                .andDo(document("auth-signup",
                        buildParams(AUTH, "회원 가입", SIGN_UP_REQUEST, SIGN_UP_RESPONSE),
                        getSignUpRequestFields(),
                        getSignUpResponseFields()
                ));
    }

    @Test
    void 회원가입_실패_아이디가_공백이면_400_BAD_REQUEST를_반환한다() throws Exception {
        SignUpRequest request = AuthDtoBuilder.signUpRequest()
                .loginId(" ")
                .build();

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("auth-signup-fail-blank-id",
                        buildErrorParams(AUTH, "회원 가입 실패 - 아이디 공백")
                ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "  ", "가나다"})
    void 회원가입_실패_비밀번호_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다(String invalidPwd) throws Exception {
        SignUpRequest request = AuthDtoBuilder.signUpRequest()
                .password(invalidPwd)
                .build();

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("auth-signup-fail-invalid-password/{index}",
                        buildErrorParams(AUTH, "회원 가입 실패 - 비밀번호 형식 오류")
                ));
    }

    @Test
    void 회원가입_실패_회원명이_공백이면_400_BAD_REQUEST를_반환한다() throws Exception {
        SignUpRequest request = AuthDtoBuilder.signUpRequest()
                .memberName(" ")
                .build();

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("auth-signup-fail-blank-memberName",
                        buildErrorParams(AUTH, "회원 가입 실패 - 회원명 공백")
                ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"adsfr", " ", "adsfwe@"})
    void 회원가입_실패_이메일_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다(String invalidEmail) throws Exception {
        SignUpRequest request = AuthDtoBuilder.signUpRequest()
                .email(invalidEmail)
                .build();

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("auth-signup-fail-invalid-email/{index}",
                        buildErrorParams(AUTH, "회원 가입 실패 - 이메일 형식 오류")
                ));
    }

    @Test
    void 회원가입_실패_전화번호_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다() throws Exception {
        SignUpRequest request = AuthDtoBuilder.signUpRequest()
                .phoneNumber("01012345678")
                .build();

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("auth-signup-fail-invalid-phoneNumber",
                        buildErrorParams(AUTH, "회원 가입 실패 - 전화번호 형식 오류")
                ));
    }

    @Test
    void 올바른_로그인_아이디와_비밀번호를_입력하면_200_OK와_함께_액세스_토큰을_반환하고_API_문서가_생성된다() throws Exception {
        LoginRequest request = AuthDtoBuilder.loginRequest().build();
        LoginResult result = new LoginResult(
                1L, "홍길동", Role.ROLE_HEADQUARTERS, "stub-token.1.ROLE_HEADQUARTERS"
        );

        given(loginUseCase.login(any())).willReturn(result);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1L))
                .andExpect(jsonPath("$.role").value("ROLE_HEADQUARTERS"))
                .andExpect(jsonPath("$.accessToken").value("stub-token.1.ROLE_HEADQUARTERS"))
                .andDo(document("auth-login-success",
                        buildParams(AUTH, "로그인", LOGIN_REQUEST, LOGIN_RESPONSE),
                        getLoginRequestFields(),
                        getLoginResponseFields()
                ));
    }

    @Test
    void 비밀번호가_공백이면_400_BAD_REQUEST를_반환한다() throws Exception {
        LoginRequest request = AuthDtoBuilder.loginRequest()
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("auth-login-fail-blank-password",
                        buildErrorParams(AUTH, "로그인 실패 - 비밀번호 공백")
                ));
    }
}
