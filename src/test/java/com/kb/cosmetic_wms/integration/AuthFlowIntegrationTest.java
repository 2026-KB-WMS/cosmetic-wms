package com.kb.cosmetic_wms.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest extends IntegrationTestSupport {

    private static final String PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtDecoder jwtDecoder;

    private record Account(long memberId, String loginId) {
    }

    private Account signUp(String role) throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String loginId = "flow-" + suffix;
        String phone = String.format("010-%04d-%04d",
                ThreadLocalRandom.current().nextInt(10000), ThreadLocalRandom.current().nextInt(10000));

        String body = mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "loginId", loginId,
                                "password", PASSWORD,
                                "role", role,
                                "memberName", "흐름테스트",
                                "email", loginId + "@example.com",
                                "phoneNumber", phone))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return new Account(objectMapper.readTree(body).get("id").asLong(), loginId);
    }

    private JsonNode login(String loginId, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("loginId", loginId, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private org.springframework.test.web.servlet.ResultActions reissue(String refreshToken) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))));
    }

    @Test
    void 가입한_회원이_로그인하면_회원_ID와_권한이_담긴_액세스_토큰을_받는다() throws Exception {
        Account account = signUp("ROLE_FRANCHISE_MANAGER");

        JsonNode tokens = login(account.loginId(), PASSWORD);
        Jwt jwt = jwtDecoder.decode(tokens.get("accessToken").asText());

        assertThat(jwt.getSubject()).isEqualTo(String.valueOf(account.memberId()));
        assertThat(jwt.<String>getClaim("role")).isEqualTo("ROLE_FRANCHISE_MANAGER");
        assertThat(tokens.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(tokens.get("refreshToken").asText()).isNotBlank();
    }

    @Test
    void 비밀번호가_틀리면_토큰을_발급하지_않는다() throws Exception {
        Account account = signUp("ROLE_WAREHOUSE_MANAGER");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "loginId", account.loginId(), "password", "Wrong-password1!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("LOGIN_FAILED"));
    }

    @Test
    void 리프레시_토큰으로_재발급하면_새_토큰_쌍이_발급되고_이전_리프레시_토큰은_재사용할_수_없다() throws Exception {
        Account account = signUp("ROLE_WAREHOUSE_MANAGER");
        String firstRefresh = login(account.loginId(), PASSWORD).get("refreshToken").asText();

        String body = reissue(firstRefresh).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String secondRefresh = objectMapper.readTree(body).get("refreshToken").asText();

        assertThat(secondRefresh).isNotEqualTo(firstRefresh);
        reissue(firstRefresh).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REFRESH_TOKEN"));
        reissue(secondRefresh).andExpect(status().isOk());
    }

    @Test
    void 로그아웃한_리프레시_토큰으로는_재발급할_수_없다() throws Exception {
        Account account = signUp("ROLE_FRANCHISE_MANAGER");
        String refresh = login(account.loginId(), PASSWORD).get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refresh))))
                .andExpect(status().isNoContent());

        reissue(refresh).andExpect(status().isUnauthorized());
    }

    @Test
    void 같은_계정으로_여러_번_로그인해도_각_세션의_리프레시_토큰은_독립적이다() throws Exception {
        Account account = signUp("ROLE_FRANCHISE_MANAGER");
        String first = login(account.loginId(), PASSWORD).get("refreshToken").asText();
        String second = login(account.loginId(), PASSWORD).get("refreshToken").asText();

        reissue(first).andExpect(status().isOk());
        reissue(second).andExpect(status().isOk());
    }
}
