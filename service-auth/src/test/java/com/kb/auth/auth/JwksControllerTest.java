package com.kb.auth.auth;

import com.kb.auth.auth.adapter.in.web.JwksController;
import com.kb.auth.global.config.JwtConfig;
import com.kb.auth.global.config.JwtProperties;
import com.kb.auth.global.config.SecurityConfig;
import com.kb.auth.global.restdocs.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.auth.global.restdocs.ApiDocs.JWKS;
import static com.kb.auth.global.restdocs.ApiSchemas.JWKS_RESPONSE;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {JwksController.class})
@Import({SecurityConfig.class, JwtConfig.class})
@EnableConfigurationProperties(JwtProperties.class)
public class JwksControllerTest extends RestDocsSupport {

    @Test
    @WithMockUser
    void JWKs_엔드포인트는_200_OK와_RSA_공개키_정보를_반환한다() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].use").value("sig"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
                .andExpect(jsonPath("$.keys[0].kid").isNotEmpty())
                .andExpect(jsonPath("$.keys[0].n").isNotEmpty())
                .andExpect(jsonPath("$.keys[0].e").isNotEmpty())
                .andDo(document("jwks",
                        buildParams(JWKS, "JWKs 공개키 조회", null, JWKS_RESPONSE),
                        createResponseFields(
                                fieldWithPath("keys").type(ARRAY).description("JWK 키 목록"),
                                fieldWithPath("keys[].kty").type(STRING).description("키 유형 (RSA)"),
                                fieldWithPath("keys[].use").type(STRING).description("키 용도 (sig: 서명 검증)"),
                                fieldWithPath("keys[].alg").type(STRING).description("서명 알고리즘 (RS256)"),
                                fieldWithPath("keys[].kid").type(STRING).description("키 식별자"),
                                fieldWithPath("keys[].n").type(STRING).description("RSA 공개키 모듈러스 (Base64url)"),
                                fieldWithPath("keys[].e").type(STRING).description("RSA 공개키 지수 (Base64url)")
                        )
                ));
    }
}
