package com.kb.auth.auth.adapter.out.token;

import com.kb.auth.auth.application.port.out.TokenIssuer;
import com.kb.auth.auth.application.port.out.TokenParser;
import com.kb.auth.global.config.JwtProperties;
import com.kb.auth.member.domain.model.Role;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenIssuer, TokenParser {

    private final JwtProperties jwtProperties;
    private final RSAPrivateKey jwtPrivateKey;
    private final RSAPublicKey jwtPublicKey;

    @Override
    public String issueAccessToken(Long memberId, Role role) {
        Date now = new Date();

        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(memberId))
                .claim("role", role.name())
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.accessTokenExpirationMs()))
                .signWith(jwtPrivateKey, Jwts.SIG.RS256)
                .compact();
    }

    @Override
    public String issueRefreshToken(Long memberId) {
        Date now = new Date();

        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(memberId))
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.refreshTokenExpirationMs()))
                .signWith(jwtPrivateKey, Jwts.SIG.RS256)
                .compact();
    }

    @Override
    public Long extractMemberId(String token) {
        String subject = Jwts.parser()
                .verifyWith(jwtPublicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return Long.parseLong(subject);
    }
}