package com.kb.cosmetic_wms.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.auth.adapter.in.web.AuthController;
import com.kb.cosmetic_wms.auth.adapter.in.web.LoginRequest;
import com.kb.cosmetic_wms.auth.adapter.in.web.RefreshTokenRequest;
import com.kb.cosmetic_wms.auth.application.port.in.LoginUseCase;
import com.kb.cosmetic_wms.auth.application.port.in.LogoutUseCase;
import com.kb.cosmetic_wms.auth.application.port.in.ReissueTokenUseCase;
import com.kb.cosmetic_wms.auth.application.port.in.TokenResult;
import com.kb.cosmetic_wms.auth.domain.exception.InvalidRefreshTokenException;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.auth.fixture.AuthDocumentUtils.getLoginRequestFields;
import static com.kb.cosmetic_wms.auth.fixture.AuthDocumentUtils.getRefreshTokenRequestFields;
import static com.kb.cosmetic_wms.auth.fixture.AuthDocumentUtils.getTokenResponseFields;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.AUTH;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.AUTH_LOGIN_REQUEST;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.AUTH_REFRESH_TOKEN_REQUEST;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.AUTH_TOKEN_RESPONSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private ReissueTokenUseCase reissueTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    private final TokenResult tokens = new TokenResult("access-token", "refresh-token", 1800);

    @Test
    void 로그인에_성공하면_200과_토큰을_반환하고_API_문서가_생성된다() throws Exception {
        given(loginUseCase.login(any())).willReturn(tokens);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin01", "password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andDo(document("auth-login",
                        buildParams(AUTH, "로그인 (토큰 발급)", AUTH_LOGIN_REQUEST, AUTH_TOKEN_RESPONSE),
                        getLoginRequestFields(),
                        getTokenResponseFields()
                ));
    }

    @Test
    void 로그인_요청의_비밀번호가_공백이면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin01", " "))))
                .andExpect(status().isBadRequest())
                .andDo(document("auth-login-fail-blank-password",
                        buildErrorParams(AUTH, "로그인 실패 - 비밀번호 공백")
                ));
    }

    @Test
    void 리프레시_토큰으로_재발급하면_200과_새_토큰_쌍을_반환하고_API_문서가_생성된다() throws Exception {
        given(reissueTokenUseCase.reissue(any())).willReturn(new TokenResult("new-access", "new-refresh", 1800));

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"))
                .andDo(document("auth-reissue",
                        buildParams(AUTH, "토큰 재발급 (리프레시 토큰 회전)", AUTH_REFRESH_TOKEN_REQUEST, AUTH_TOKEN_RESPONSE),
                        getRefreshTokenRequestFields(),
                        getTokenResponseFields()
                ));
    }

    @Test
    void 유효하지_않은_리프레시_토큰으로_재발급하면_401을_반환한다() throws Exception {
        given(reissueTokenUseCase.reissue(any())).willThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("expired"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REFRESH_TOKEN"))
                .andDo(document("auth-reissue-fail-invalid-token",
                        buildErrorParams(AUTH, "토큰 재발급 실패 - 유효하지 않은 리프레시 토큰")
                ));
    }

    @Test
    void 로그아웃하면_204를_반환하고_리프레시_토큰을_폐기한다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("refresh-token"))))
                .andExpect(status().isNoContent())
                .andDo(document("auth-logout",
                        buildParams(AUTH, "로그아웃 (리프레시 토큰 폐기)", AUTH_REFRESH_TOKEN_REQUEST, null),
                        getRefreshTokenRequestFields()
                ));

        verify(logoutUseCase).logout(any());
    }

    @Test
    void 로그아웃_요청의_리프레시_토큰이_공백이면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(""))))
                .andExpect(status().isBadRequest());
    }
}
