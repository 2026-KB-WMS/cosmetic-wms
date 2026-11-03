package com.kb.cosmetic_wms.integration;

import com.kb.cosmetic_wms.global.config.JwtConfig;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.member.domain.model.Role;
import com.kb.cosmetic_wms.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationMatrixTest extends IntegrationTestSupport {

    private static final String HQ = "ROLE_HEADQUARTERS";
    private static final String WH = "ROLE_WAREHOUSE_MANAGER";
    private static final String FR = "ROLE_FRANCHISE_MANAGER";
    private static final Set<String> ALL_ROLES = Set.of(HQ, WH, FR);
    private static final Set<String> NONE = Set.of();

    private static final String UNKNOWN_ID = "987654321";

    private record Endpoint(HttpMethod method, String pattern, Set<String> allowedRoles, boolean isPublic) {

        static Endpoint publicApi(HttpMethod method, String pattern) {
            return new Endpoint(method, pattern, NONE, true);
        }

        static Endpoint of(HttpMethod method, String pattern, String... roles) {
            return new Endpoint(method, pattern, Set.of(roles), false);
        }

        String path() {
            return pattern.replaceAll("\\{[^}]+}", UNKNOWN_ID);
        }

        String key() {
            return method + " " + pattern;
        }
    }

    private static final List<Endpoint> MATRIX = List.of(
            // 공개
            Endpoint.publicApi(HttpMethod.POST, "/api/v1/members/signup"),
            Endpoint.publicApi(HttpMethod.POST, "/api/v1/auth/login"),
            Endpoint.publicApi(HttpMethod.POST, "/api/v1/auth/reissue"),
            Endpoint.publicApi(HttpMethod.POST, "/api/v1/auth/logout"),

            // 상품 마스터: 조회는 로그인한 모두, 등록·수정·삭제는 본사
            Endpoint.of(HttpMethod.GET, "/api/v1/products", HQ, WH, FR),
            Endpoint.of(HttpMethod.GET, "/api/v1/products/{productId}", HQ, WH, FR),
            Endpoint.of(HttpMethod.POST, "/api/v1/products", HQ),
            Endpoint.of(HttpMethod.PUT, "/api/v1/products/{productId}", HQ),
            Endpoint.of(HttpMethod.DELETE, "/api/v1/products/{productId}", HQ),
            Endpoint.of(HttpMethod.GET, "/api/v1/categories", HQ, WH, FR),
            Endpoint.of(HttpMethod.GET, "/api/v1/categories/{categoryId}", HQ, WH, FR),
            Endpoint.of(HttpMethod.POST, "/api/v1/categories", HQ),
            Endpoint.of(HttpMethod.DELETE, "/api/v1/categories/{categoryId}", HQ),
            Endpoint.of(HttpMethod.GET, "/api/v1/product-types", HQ, WH, FR),
            Endpoint.of(HttpMethod.GET, "/api/v1/product-types/{productTypeId}", HQ, WH, FR),
            Endpoint.of(HttpMethod.POST, "/api/v1/product-types", HQ),
            Endpoint.of(HttpMethod.DELETE, "/api/v1/product-types/{productTypeId}", HQ),

            // 가맹점·협력사·창고 마스터
            Endpoint.of(HttpMethod.GET, "/api/v1/stores/{storeId}", HQ, WH, FR),
            Endpoint.of(HttpMethod.POST, "/api/v1/stores", HQ),
            Endpoint.of(HttpMethod.GET, "/api/v1/partners/{partnerId}", HQ, WH),
            Endpoint.of(HttpMethod.POST, "/api/v1/partners", HQ),
            Endpoint.of(HttpMethod.GET, "/api/v1/storages/warehouses", HQ, WH),
            Endpoint.of(HttpMethod.GET, "/api/v1/storages/warehouses/{warehouseId}", HQ, WH),
            Endpoint.of(HttpMethod.POST, "/api/v1/storages/warehouses", HQ),
            Endpoint.of(HttpMethod.POST, "/api/v1/storages/warehouses/{warehouseId}/sections", HQ),

            // 로트
            Endpoint.of(HttpMethod.GET, "/api/v1/lots", HQ, WH),
            Endpoint.of(HttpMethod.GET, "/api/v1/lots/{lotId}", HQ, WH),
            Endpoint.of(HttpMethod.POST, "/api/v1/lots", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/lots/{lotId}/status", HQ, WH),
            Endpoint.of(HttpMethod.DELETE, "/api/v1/lots/{lotId}", HQ, WH),

            // 입고·검사
            Endpoint.of(HttpMethod.GET, "/api/v1/inbounds/{inboundId}", HQ, WH),
            Endpoint.of(HttpMethod.POST, "/api/v1/inbounds", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/inbounds/{inboundId}/receive", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/inbounds/{inboundId}/cancel", HQ, WH),
            Endpoint.of(HttpMethod.GET, "/api/v1/quality-inspections", HQ, WH),
            Endpoint.of(HttpMethod.GET, "/api/v1/quality-inspections/{inspectionId}", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/quality-inspections/{inspectionId}/start", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/quality-inspections/{inspectionId}/complete", HQ, WH),

            // 재고
            Endpoint.of(HttpMethod.GET, "/api/v1/inventories", HQ, WH),
            Endpoint.of(HttpMethod.GET, "/api/v1/inventories/{inventoryId}", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/inventories/{inventoryId}/allocate", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/inventories/{inventoryId}/unallocate", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/inventories/{inventoryId}/move/start", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/inventories/{inventoryId}/move/finish", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/inventories/{inventoryId}/quality/inspect", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/inventories/{inventoryId}/quality/hold", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/inventories/{inventoryId}/quality/restore", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/inventories/{inventoryId}/quality/discard", HQ, WH),

            // 출고
            Endpoint.of(HttpMethod.POST, "/api/v1/outbounds", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/outbounds/{outboundId}/allocate", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/outbounds/{outboundId}/process", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/outbounds/{outboundId}/ship", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/outbounds/{outboundId}/cancel", HQ, WH),

            // 발주: 가맹점이 발주·확정·취소·수령, 창고가 준비·출하
            Endpoint.of(HttpMethod.POST, "/api/v1/orders", HQ, FR),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/orders/{orderId}/confirm", HQ, FR),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/orders/{orderId}/cancel", HQ, FR),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/orders/{orderId}/deliver", HQ, FR),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/orders/{orderId}/prepare", HQ, WH),
            Endpoint.of(HttpMethod.PATCH, "/api/v1/orders/{orderId}/ship", HQ, WH),

            // 운영 관리: 실패한 이벤트 재처리는 본사만
            Endpoint.of(HttpMethod.GET, "/api/v1/admin/failed-inspections", HQ),
            Endpoint.of(HttpMethod.POST, "/api/v1/admin/failed-inspections/{failedEventId}/retry", HQ),
            Endpoint.of(HttpMethod.POST, "/api/v1/admin/failed-inspections/{failedEventId}/discard", HQ)
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    private String bearer(String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject("1")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(600))
                .claim("role", role)
                .build();
        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return "Bearer " + token;
    }

    private int statusOf(Endpoint endpoint, String authorization) throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(endpoint.method(), endpoint.path());
        if (endpoint.method() != HttpMethod.GET) {
            request.contentType(MediaType.APPLICATION_JSON).content("{}");
        }
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        return mockMvc.perform(request).andReturn().getResponse().getStatus();
    }

    static Stream<Endpoint> endpoints() {
        return MATRIX.stream();
    }

    @Test
    void 매트릭스는_실제_컨트롤러_매핑_전체와_정확히_일치한다() {
        Set<String> actual = new TreeSet<>();
        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            Set<String> patterns = info.getPathPatternsCondition() == null ? Set.of()
                    : info.getPathPatternsCondition().getPatternValues();
            for (String pattern : patterns) {
                if (pattern.startsWith("/api/")) {
                    for (var method : info.getMethodsCondition().getMethods()) {
                        actual.add(method.name() + " " + pattern);
                    }
                }
            }
        }
        Set<String> expected = MATRIX.stream().map(Endpoint::key).collect(Collectors.toCollection(TreeSet::new));

        assertThat(actual).as("컨트롤러에 매핑된 API와 권한 매트릭스가 같아야 한다").isEqualTo(expected);
    }

    @Test
    void 보안_설정의_역할_이름은_Role_enum과_일치한다() {
        Set<String> configured = Set.of(SecurityConfig.HEADQUARTERS, SecurityConfig.WAREHOUSE_MANAGER, SecurityConfig.FRANCHISE_MANAGER);
        Set<String> enumRoles = Arrays.stream(Role.values()).map(r -> r.name().replaceFirst("^ROLE_", ""))
                .collect(Collectors.toSet());

        assertThat(configured).isEqualTo(enumRoles);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void 토큰이_없으면_공개_API만_접근할_수_있다(Endpoint endpoint) throws Exception {
        int status = statusOf(endpoint, null);

        if (endpoint.isPublic()) {
            assertThat(status).isNotIn(401, 403);
        } else {
            assertThat(status).isEqualTo(401);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void 허용된_역할만_접근할_수_있고_나머지는_403이다(Endpoint endpoint) throws Exception {
        for (String role : ALL_ROLES) {
            int status = statusOf(endpoint, bearer(role));

            if (endpoint.isPublic() || endpoint.allowedRoles().contains(role)) {
                assertThat(status).as("%s 가 %s 호출", role, endpoint.key()).isNotIn(401, 403);
            } else {
                assertThat(status).as("%s 가 %s 호출", role, endpoint.key()).isEqualTo(403);
            }
        }
    }

    @Test
    void 본사_관리자는_공개_API를_포함한_모든_API에_접근할_수_있다() throws Exception {
        for (Endpoint endpoint : MATRIX) {
            assertThat(endpoint.isPublic() || endpoint.allowedRoles().contains(HQ))
                    .as("본사 관리자가 접근할 수 없는 API: %s", endpoint.key()).isTrue();
        }
    }
}
