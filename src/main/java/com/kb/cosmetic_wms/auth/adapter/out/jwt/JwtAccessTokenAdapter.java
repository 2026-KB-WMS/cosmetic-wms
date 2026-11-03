package com.kb.cosmetic_wms.auth.adapter.out.jwt;

import com.kb.cosmetic_wms.auth.application.port.out.AccessTokenPort;
import com.kb.cosmetic_wms.auth.application.port.out.IssuedAccessToken;
import com.kb.cosmetic_wms.auth.application.port.out.MemberIdentity;
import com.kb.cosmetic_wms.global.config.JwtConfig;
import com.kb.cosmetic_wms.global.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenAdapter implements AccessTokenPort {

    public static final String ROLE_CLAIM = "role";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    @Override
    public IssuedAccessToken issue(MemberIdentity identity) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject(String.valueOf(identity.memberId()))
                .issuedAt(now)
                .expiresAt(now.plus(properties.accessTokenTtl()))
                .id(UUID.randomUUID().toString())
                .claim(ROLE_CLAIM, identity.role())
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).getTokenValue();

        return new IssuedAccessToken(token, properties.accessTokenTtl().toSeconds());
    }
}
