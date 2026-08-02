package com.kb.auth.auth.adapter.in.web;

import com.kb.auth.global.config.JwtProperties;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/.well-known")
public class JwksController {

    private final JwksResponse jwksResponse;

    public JwksController(RSAPublicKey jwtPublicKey, JwtProperties jwtProperties) {
        this.jwksResponse = buildJwksResponse(jwtPublicKey, jwtProperties.kid());
    }

    @GetMapping("/jwks.json")
    public ResponseEntity<JwksResponse> jwks() {
        return ResponseEntity.ok(jwksResponse);
    }

    private static JwksResponse buildJwksResponse(RSAPublicKey publicKey, String kid) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String n = encoder.encodeToString(toUnsignedBytes(publicKey.getModulus()));
        String e = encoder.encodeToString(toUnsignedBytes(publicKey.getPublicExponent()));
        JwkKey key = new JwkKey("RSA", "sig", "RS256", kid, n, e);
        return new JwksResponse(List.of(key));
    }

    // BigInteger.toByteArray()는 2's complement로 MSB가 1이면 leading zero를 추가한다.
    // JWK 스펙(RFC 7517)은 부호 없는 바이트 배열을 요구하므로 제거한다.
    private static byte[] toUnsignedBytes(BigInteger value) {
        byte[] bytes = value.toByteArray();
        return bytes[0] == 0 ? java.util.Arrays.copyOfRange(bytes, 1, bytes.length) : bytes;
    }

    public record JwksResponse(List<JwkKey> keys) {}

    public record JwkKey(String kty, String use, String alg, String kid, String n, String e) {}
}
