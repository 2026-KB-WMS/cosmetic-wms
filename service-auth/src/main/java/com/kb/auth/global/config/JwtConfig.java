package com.kb.auth.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public RSAPrivateKey jwtPrivateKey(JwtProperties jwtProperties) throws Exception {
        ClassPathResource resource = new ClassPathResource(jwtProperties.privateKeyPath());
        String pem = new String(resource.getInputStream().readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    @Bean
    public RSAPublicKey jwtPublicKey(RSAPrivateKey jwtPrivateKey) throws Exception {
        if (!(jwtPrivateKey instanceof RSAPrivateCrtKey crtKey)) {
            throw new IllegalStateException("RSA private key does not contain CRT parameters — cannot derive public key");
        }
        RSAPublicKeySpec spec = new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent());
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}