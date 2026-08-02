package com.kb.auth.auth.adapter.out.token;

import com.kb.auth.auth.application.port.out.TokenIssuer;
import com.kb.auth.auth.application.port.out.TokenParser;
import com.kb.auth.global.config.JwtProperties;
import com.kb.auth.member.domain.model.Role;
import com.kb.common.security.AuthenticatedMember;
import com.kb.common.security.JwtClaimExtractor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenIssuer, TokenParser, JwtClaimExtractor {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final JwtProperties jwtProperties;
    private final RSAPrivateKey jwtPrivateKey;
    private final RSAPublicKey jwtPublicKey;

    @Override
    public String issueAccessToken(Long memberId, Role role) {
        Date now = new Date();

        return Jwts.builder()
                .header().keyId(jwtProperties.kid()).and()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(memberId))
                .claim("role", role.name())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.accessTokenExpirationMs()))
                .signWith(jwtPrivateKey, Jwts.SIG.RS256)
                .compact();
    }

    @Override
    public String issueRefreshToken(Long memberId) {
        Date now = new Date();

        return Jwts.builder()
                .header().keyId(jwtProperties.kid()).and()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(memberId))
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.refreshTokenExpirationMs()))
                .signWith(jwtPrivateKey, Jwts.SIG.RS256)
                .compact();
    }

    @Override
    public Optional<Long> extractMemberId(String token) {
        return parseClaims(token)
                .filter(claims -> TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class)))
                .map(claims -> Long.parseLong(claims.getSubject()));
    }

    @Override
    public Optional<AuthenticatedMember> extract(String token) {
        return parseClaims(token)
                .filter(claims -> TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class)))
                .flatMap(claims -> {
                    String role = claims.get("role", String.class);
                    if (role == null) {
                        return Optional.empty();
                    }
                    return Optional.of(new AuthenticatedMember(Long.parseLong(claims.getSubject()), role));
                });
    }

    private Optional<Claims> parseClaims(String token) {
        try {
            return Optional.of(Jwts.parser()
                    .verifyWith(jwtPublicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
