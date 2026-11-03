package com.kb.cosmetic_wms.integration;

import com.kb.cosmetic_wms.global.config.JwtConfig;
import com.kb.cosmetic_wms.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityBehaviorIntegrationTest extends IntegrationTestSupport {

    private static final String PROTECTED_API = "/api/v1/products";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private HealthEndpoint healthEndpoint;

    private String token(String role, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject("1")
                .issuedAt(expiresAt.minusSeconds(600))
                .expiresAt(expiresAt)
                .claim("role", role)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    @Test
    void 토큰이_없으면_401과_인증_필요_에러_코드를_반환한다() throws Exception {
        mockMvc.perform(get(PROTECTED_API))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED"));
    }

    @Test
    void 만료된_토큰이면_401과_토큰_오류_에러_코드를_반환해_재발급을_유도한다() throws Exception {
        String expired = token("ROLE_HEADQUARTERS", Instant.now().minusSeconds(5));

        mockMvc.perform(get(PROTECTED_API).header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));
    }

    @Test
    void 위조된_토큰이면_401을_반환한다() throws Exception {
        String valid = token("ROLE_HEADQUARTERS", Instant.now().plusSeconds(600));
        String tampered = valid.substring(0, valid.length() - 2) + (valid.endsWith("AA") ? "BB" : "AA");

        mockMvc.perform(get(PROTECTED_API).header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));
    }

    @Test
    void 알_수_없는_역할의_토큰이면_403과_접근_거부_에러_코드를_반환한다() throws Exception {
        String unknownRole = token("ROLE_SOMETHING_ELSE", Instant.now().plusSeconds(600));

        mockMvc.perform(get(PROTECTED_API).header("Authorization", "Bearer " + unknownRole))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void 허용된_역할의_유효한_토큰이면_접근할_수_있다() throws Exception {
        String valid = token("ROLE_FRANCHISE_MANAGER", Instant.now().plusSeconds(600));

        mockMvc.perform(get(PROTECTED_API).header("Authorization", "Bearer " + valid))
                .andExpect(status().isOk());
    }

    @Test
    void 규칙에_없는_경로는_본사_관리자라도_기본적으로_거부한다() throws Exception {
        String valid = token("ROLE_HEADQUARTERS", Instant.now().plusSeconds(600));

        mockMvc.perform(get("/api/v1/no-such-endpoint").header("Authorization", "Bearer " + valid))
                .andExpect(status().isForbidden());
    }

    @Test
    void Swagger_문서_경로는_인증_없이_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void health는_인증_없이_접근할_수_있고_세부_정보는_노출하지_않는다() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void liveness와_readiness_프로브는_인증_없이_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void 종합_health는_DB와_Redis_상태를_포함한다() {
        CompositeHealth health = (CompositeHealth) healthEndpoint.health();

        assertThat(health.getComponents()).containsKeys("db", "redis");
        assertThat(health.getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    void health_외의_actuator_경로는_인증_없이는_401이고_본사_관리자도_거부한다() throws Exception {
        String hq = token("ROLE_HEADQUARTERS", Instant.now().plusSeconds(600));

        mockMvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/env").header("Authorization", "Bearer " + hq))
                .andExpect(status().isForbidden());
    }
}
