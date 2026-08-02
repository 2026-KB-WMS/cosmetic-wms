package com.kb.auth.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.auth.auth.adapter.in.web.AuthController;
import com.kb.auth.auth.adapter.in.web.dto.LoginRequest;
import com.kb.auth.auth.adapter.in.web.dto.LogoutRequest;
import com.kb.auth.auth.adapter.in.web.dto.ReissueRequest;
import com.kb.auth.auth.adapter.in.web.dto.SignUpRequest;
import com.kb.auth.auth.application.port.in.LoginUseCase;
import com.kb.auth.auth.application.port.in.LogoutUseCase;
import com.kb.auth.auth.application.port.in.ReissueTokenUseCase;
import com.kb.auth.auth.application.port.in.SignUpUseCase;
import com.kb.auth.auth.application.port.in.dto.LoginResult;
import com.kb.auth.auth.application.port.in.dto.ReissueResult;
import com.kb.auth.auth.application.port.in.dto.SignUpResult;
import com.kb.auth.auth.domain.exception.DuplicateLoginIdException;
import com.kb.auth.auth.domain.exception.InvalidRefreshTokenException;
import com.kb.auth.auth.domain.exception.LoginFailedException;
import com.kb.auth.global.config.SecurityConfig;
import com.kb.auth.global.restdocs.RestDocsSupport;
import com.kb.auth.member.domain.model.Role;
import com.kb.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.auth.global.restdocs.ApiDocs.AUTH;
import static com.kb.auth.global.restdocs.ApiSchemas.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class AuthControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SignUpUseCase signUpUseCase;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private ReissueTokenUseCase reissueTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @Nested
    class 회원가입 {

        @Test
        @WithMockUser
        void 올바른_회원정보로_가입하면_201_CREATED와_SignUpResponse를_반환한다() throws Exception {
            // given
            SignUpRequest request = validSignUpRequest();
            given(signUpUseCase.signUp(any())).willReturn(signUpResult());

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.memberId").value(1L))
                    .andExpect(jsonPath("$.loginId").value("user12345"))
                    .andExpect(jsonPath("$.role").value("ROLE_HEADQUARTERS"))
                    .andDo(document("auth-signup-success",
                            buildParams(AUTH, "회원가입", SIGN_UP_REQUEST, SIGN_UP_RESPONSE),
                            createRequestFields(getSignUpRequestFields()),
                            createResponseFields(getSignUpResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void loginId가_없으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            SignUpRequest request = new SignUpRequest(
                    null, "Password1!", Role.ROLE_HEADQUARTERS, "홍길동", "test@example.com", "010-1234-5678"
            );

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("auth-signup-fail-no-login-id",
                            buildErrorParams(AUTH, "회원가입"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void loginId가_최소_길이_미만이면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            SignUpRequest request = new SignUpRequest(
                    "abc", "Password1!", Role.ROLE_HEADQUARTERS, "홍길동", "test@example.com", "010-1234-5678"
            );

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("auth-signup-fail-login-id-too-short",
                            buildErrorParams(AUTH, "회원가입"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void password_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given: 특수문자 없는 비밀번호
            SignUpRequest request = new SignUpRequest(
                    "user12345", "password1", Role.ROLE_HEADQUARTERS, "홍길동", "test@example.com", "010-1234-5678"
            );

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("auth-signup-fail-invalid-password",
                            buildErrorParams(AUTH, "회원가입"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 이메일_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            SignUpRequest request = new SignUpRequest(
                    "user12345", "Password1!", Role.ROLE_HEADQUARTERS, "홍길동", "not-an-email", "010-1234-5678"
            );

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("auth-signup-fail-invalid-email",
                            buildErrorParams(AUTH, "회원가입"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 이미_존재하는_loginId로_가입하면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            SignUpRequest request = validSignUpRequest();
            given(signUpUseCase.signUp(any())).willThrow(new DuplicateLoginIdException());

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("DUPLICATE_LOGIN_ID"))
                    .andDo(document("auth-signup-fail-duplicate-login-id",
                            buildErrorParams(AUTH, "회원가입"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 로그인 {

        @Test
        @WithMockUser
        void 올바른_아이디와_비밀번호로_로그인하면_200_OK와_토큰을_반환한다() throws Exception {
            // given
            LoginRequest request = new LoginRequest("user12345", "Password1!");
            given(loginUseCase.login(any())).willReturn(loginResult());

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(1L))
                    .andExpect(jsonPath("$.accessToken").value("access.token"))
                    .andDo(document("auth-login-success",
                            buildParams(AUTH, "로그인", LOGIN_REQUEST, LOGIN_RESPONSE),
                            createRequestFields(getLoginRequestFields()),
                            createResponseFields(getLoginResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void loginId가_없으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            LoginRequest request = new LoginRequest(null, "Password1!");

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("auth-login-fail-no-login-id",
                            buildErrorParams(AUTH, "로그인"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void password가_없으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            LoginRequest request = new LoginRequest("user12345", null);

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("auth-login-fail-no-password",
                            buildErrorParams(AUTH, "로그인"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 아이디_또는_비밀번호가_일치하지_않으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            LoginRequest request = new LoginRequest("user12345", "WrongPass1!");
            given(loginUseCase.login(any())).willThrow(new LoginFailedException());

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("LOGIN_FAILED"))
                    .andDo(document("auth-login-fail-invalid-credentials",
                            buildErrorParams(AUTH, "로그인"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 토큰_재발급 {

        @Test
        @WithMockUser
        void 유효한_리프레시_토큰으로_재발급하면_200_OK와_새_토큰을_반환한다() throws Exception {
            // given
            ReissueRequest request = new ReissueRequest("valid.refresh.token");
            given(reissueTokenUseCase.reissue(any())).willReturn(reissueResult());

            // when & then
            mockMvc.perform(post("/api/v1/auth/reissue")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(1L))
                    .andExpect(jsonPath("$.accessToken").value("new.access.token"))
                    .andExpect(jsonPath("$.refreshToken").value("new.refresh.token"))
                    .andDo(document("auth-reissue-success",
                            buildParams(AUTH, "토큰 재발급", REISSUE_REQUEST, REISSUE_RESPONSE),
                            createRequestFields(getReissueRequestFields()),
                            createResponseFields(getReissueResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void refreshToken이_없으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            ReissueRequest request = new ReissueRequest(null);

            // when & then
            mockMvc.perform(post("/api/v1/auth/reissue")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("auth-reissue-fail-no-token",
                            buildErrorParams(AUTH, "토큰 재발급"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 유효하지_않은_리프레시_토큰으로_재발급하면_401_UNAUTHORIZED를_반환한다() throws Exception {
            // given
            ReissueRequest request = new ReissueRequest("invalid.refresh.token");
            willThrow(new InvalidRefreshTokenException()).given(reissueTokenUseCase).reissue(any());

            // when & then
            mockMvc.perform(post("/api/v1/auth/reissue")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REFRESH_TOKEN"))
                    .andDo(document("auth-reissue-fail-invalid-token",
                            buildErrorParams(AUTH, "토큰 재발급"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 로그아웃 {

        @Test
        @WithMockUser
        void 유효한_리프레시_토큰으로_로그아웃하면_204_NO_CONTENT를_반환한다() throws Exception {
            // given
            LogoutRequest request = new LogoutRequest("valid.refresh.token");

            // when & then
            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andDo(document("auth-logout-success",
                            buildParams(AUTH, "로그아웃", LOGOUT_REQUEST, null),
                            createRequestFields(getLogoutRequestFields())
                    ));
        }

        @Test
        @WithMockUser
        void refreshToken이_없으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            LogoutRequest request = new LogoutRequest(null);

            // when & then
            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("auth-logout-fail-no-token",
                            buildErrorParams(AUTH, "로그아웃"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 유효하지_않은_리프레시_토큰으로_로그아웃하면_401_UNAUTHORIZED를_반환한다() throws Exception {
            // given
            LogoutRequest request = new LogoutRequest("malformed.token");
            willThrow(new InvalidRefreshTokenException()).given(logoutUseCase).logout(any());

            // when & then
            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REFRESH_TOKEN"))
                    .andDo(document("auth-logout-fail-invalid-token",
                            buildErrorParams(AUTH, "로그아웃"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // ── Fixtures ──

    private static SignUpRequest validSignUpRequest() {
        return new SignUpRequest(
                "user12345", "Password1!", Role.ROLE_HEADQUARTERS, "홍길동", "test@example.com", "010-1234-5678"
        );
    }

    private static SignUpResult signUpResult() {
        return new SignUpResult(1L, "user12345", Role.ROLE_HEADQUARTERS, "홍길동", "test@example.com", "010-1234-5678");
    }

    private static LoginResult loginResult() {
        return new LoginResult(1L, "홍길동", Role.ROLE_HEADQUARTERS, "access.token", "refresh.token");
    }

    private static ReissueResult reissueResult() {
        return new ReissueResult(1L, "new.access.token", "new.refresh.token");
    }

    // ── Field Descriptors ──

    private static FieldDescriptor[] getSignUpRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("loginId").description("로그인 아이디 (5~50자)"),
                fieldWithPath("password").description("비밀번호 (8~50자, 영문·숫자·특수문자 포함)"),
                fieldWithPath("role").description("회원 역할 (ROLE_HEADQUARTERS / ROLE_WAREHOUSE_MANAGER / ROLE_FRANCHISE_MANAGER)"),
                fieldWithPath("memberName").description("회원 이름"),
                fieldWithPath("email").description("이메일 주소"),
                fieldWithPath("phoneNumber").description("전화번호 (한국 형식, 예: 010-1234-5678)")
        };
    }

    private static FieldDescriptor[] getSignUpResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("memberId").description("회원 ID"),
                fieldWithPath("loginId").description("로그인 아이디"),
                fieldWithPath("role").description("회원 역할"),
                fieldWithPath("memberName").description("회원 이름"),
                fieldWithPath("email").description("이메일 주소"),
                fieldWithPath("phoneNumber").description("전화번호")
        };
    }

    private static FieldDescriptor[] getLoginRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("loginId").description("로그인 아이디"),
                fieldWithPath("password").description("비밀번호")
        };
    }

    private static FieldDescriptor[] getLoginResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("memberId").description("회원 ID"),
                fieldWithPath("memberName").description("회원 이름"),
                fieldWithPath("role").description("회원 역할"),
                fieldWithPath("accessToken").description("JWT 액세스 토큰"),
                fieldWithPath("refreshToken").description("JWT 리프레시 토큰")
        };
    }

    private static FieldDescriptor[] getReissueRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("refreshToken").description("리프레시 토큰")
        };
    }

    private static FieldDescriptor[] getLogoutRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("refreshToken").description("리프레시 토큰")
        };
    }

    private static FieldDescriptor[] getReissueResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("memberId").description("회원 ID"),
                fieldWithPath("accessToken").description("새로 발급된 JWT 액세스 토큰"),
                fieldWithPath("refreshToken").description("새로 발급된 JWT 리프레시 토큰")
        };
    }
}
