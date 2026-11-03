package com.kb.cosmetic_wms.auth;

import com.kb.cosmetic_wms.auth.adapter.out.jwt.JwtAccessTokenAdapter;
import com.kb.cosmetic_wms.auth.application.port.out.IssuedAccessToken;
import com.kb.cosmetic_wms.auth.application.port.out.MemberIdentity;
import com.kb.cosmetic_wms.global.config.JwtConfig;
import com.kb.cosmetic_wms.global.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtAccessTokenAdapterTest {

    private static final String SECRET = "unit-test-secret-key-which-is-long-enough-0123456789";

    private final JwtProperties properties =
            new JwtProperties(SECRET, Duration.ofMinutes(30), Duration.ofDays(14));
    private final JwtConfig config = new JwtConfig();
    private final JwtEncoder encoder = config.jwtEncoder(properties);
    private final JwtDecoder decoder = config.jwtDecoder(properties);
    private final JwtAccessTokenAdapter adapter = new JwtAccessTokenAdapter(encoder, properties);

    @Test
    void 발급한_토큰은_검증되고_회원_ID와_권한을_담고_있다() {
        // when
        IssuedAccessToken issued = adapter.issue(new MemberIdentity(7L, "ROLE_HEADQUARTERS"));
        Jwt jwt = decoder.decode(issued.token());

        // then
        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.<String>getClaim("role")).isEqualTo("ROLE_HEADQUARTERS");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(JwtConfig.ISSUER);
        assertThat(jwt.getId()).isNotBlank();
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(30));
        assertThat(issued.expiresInSeconds()).isEqualTo(1800);
    }

    @Test
    void 만료된_토큰은_거부한다() {
        // given
        String expired = encode(SECRET, JwtConfig.ISSUER, Instant.now().minusSeconds(10));

        // when & then
        assertThatThrownBy(() -> decoder.decode(expired)).isInstanceOf(BadJwtException.class);
    }

    @Test
    void 다른_키로_서명한_토큰은_거부한다() {
        // given
        String forged = encode("another-secret-key-which-is-long-enough-9876543210", JwtConfig.ISSUER,
                Instant.now().plusSeconds(600));

        // when & then
        assertThatThrownBy(() -> decoder.decode(forged)).isInstanceOf(BadJwtException.class);
    }

    @Test
    void 발급자가_다른_토큰은_거부한다() {
        // given
        String foreign = encode(SECRET, "someone-else", Instant.now().plusSeconds(600));

        // when & then
        assertThatThrownBy(() -> decoder.decode(foreign)).isInstanceOf(BadJwtException.class);
    }

    @Test
    void 비밀키가_32바이트_미만이면_설정_자체를_거부한다() {
        assertThatThrownBy(() -> new JwtProperties("short", Duration.ofMinutes(1), Duration.ofDays(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    private String encode(String secret, String issuer, Instant expiresAt) {
        JwtEncoder other = config.jwtEncoder(new JwtProperties(secret, Duration.ofMinutes(30), Duration.ofDays(14)));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject("7")
                .issuedAt(expiresAt.minusSeconds(600))
                .expiresAt(expiresAt)
                .claim("role", "ROLE_HEADQUARTERS")
                .build();
        return other.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
