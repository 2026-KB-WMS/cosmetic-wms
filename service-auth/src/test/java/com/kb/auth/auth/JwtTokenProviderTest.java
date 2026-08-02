package com.kb.auth.auth;

import com.kb.auth.auth.adapter.out.token.JwtTokenProvider;
import com.kb.auth.global.config.JwtProperties;
import com.kb.auth.member.domain.model.Role;
import com.kb.common.security.AuthenticatedMember;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtTokenProviderTest {

    private static JwtTokenProvider provider;
    private static JwtTokenProvider expiredProvider;
    private static JwtTokenProvider foreignKeyProvider;

    @BeforeAll
    static void setUp() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        JwtProperties props = new JwtProperties("keys/private.pem", 300_000L, 1_209_600_000L, "test-issuer", "test-kid");
        JwtProperties expiredProps = new JwtProperties("keys/private.pem", -1_000L, -1_000L, "test-issuer", "test-kid");

        provider = new JwtTokenProvider(props, privateKey, publicKey);
        expiredProvider = new JwtTokenProvider(expiredProps, privateKey, publicKey);

        // 다른 키 쌍으로 서명된 토큰 검증용
        KeyPair foreignKeyPair = generateRsaKeyPair();
        foreignKeyProvider = new JwtTokenProvider(
                props,
                (RSAPrivateKey) foreignKeyPair.getPrivate(),
                (RSAPublicKey) foreignKeyPair.getPublic()
        );
    }

    private static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    @Nested
    class AccessToken_발행 {

        @Test
        void 유효한_accessToken을_발행한다() {
            String token = provider.issueAccessToken(1L, Role.ROLE_HEADQUARTERS);

            assertThat(token).isNotBlank();
        }

        @Test
        void accessToken은_REFRESH_타입이_아니므로_extractMemberId는_empty를_반환한다() {
            String token = provider.issueAccessToken(42L, Role.ROLE_HEADQUARTERS);

            assertThat(provider.extractMemberId(token)).isEmpty();
        }

        @Test
        void accessToken에서_role을_포함한_인증_정보를_추출할_수_있다() {
            String token = provider.issueAccessToken(1L, Role.ROLE_HEADQUARTERS);

            assertThat(provider.extract(token))
                    .isPresent()
                    .get()
                    .satisfies(member -> {
                        assertThat(member.memberId()).isEqualTo(1L);
                        assertThat(member.role()).isEqualTo("ROLE_HEADQUARTERS");
                    });
        }
    }

    @Nested
    class RefreshToken_발행 {

        @Test
        void 유효한_refreshToken을_발행한다() {
            String token = provider.issueRefreshToken(1L);

            assertThat(token).isNotBlank();
        }

        @Test
        void refreshToken에서_memberId를_추출할_수_있다() {
            String token = provider.issueRefreshToken(99L);

            assertThat(provider.extractMemberId(token)).contains(99L);
        }

        @Test
        void refreshToken은_role_클레임이_없으므로_extract는_empty를_반환한다() {
            // refresh token은 role 클레임을 포함하지 않으므로 필터 인증에 사용 불가
            String token = provider.issueRefreshToken(1L);

            assertThat(provider.extract(token)).isEmpty();
        }
    }

    @Nested
    class memberId_추출 {

        @Test
        void 만료된_토큰은_empty를_반환한다() {
            String expiredToken = expiredProvider.issueAccessToken(1L, Role.ROLE_HEADQUARTERS);

            assertThat(provider.extractMemberId(expiredToken)).isEmpty();
        }

        @Test
        void 변조된_토큰은_empty를_반환한다() {
            assertThat(provider.extractMemberId("invalid.token.value")).isEmpty();
        }

        @Test
        void 빈_문자열_토큰은_empty를_반환한다() {
            assertThat(provider.extractMemberId("")).isEmpty();
        }

        @Test
        void 다른_키로_서명된_토큰은_empty를_반환한다() {
            String foreignToken = foreignKeyProvider.issueAccessToken(1L, Role.ROLE_HEADQUARTERS);

            assertThat(provider.extractMemberId(foreignToken)).isEmpty();
        }
    }

    @Nested
    class 인증_정보_추출 {

        @Test
        void 만료된_accessToken은_empty를_반환한다() {
            String expiredToken = expiredProvider.issueAccessToken(1L, Role.ROLE_HEADQUARTERS);

            assertThat(provider.extract(expiredToken)).isEmpty();
        }

        @Test
        void 변조된_토큰은_empty를_반환한다() {
            assertThat(provider.extract("bad.token.here")).isEmpty();
        }

        @Test
        void 다른_키로_서명된_토큰은_empty를_반환한다() {
            String foreignToken = foreignKeyProvider.issueAccessToken(1L, Role.ROLE_HEADQUARTERS);

            assertThat(provider.extract(foreignToken)).isEmpty();
        }

        @Test
        void 서로_다른_회원의_토큰은_각각_올바른_memberId를_반환한다() {
            String tokenA = provider.issueAccessToken(10L, Role.ROLE_HEADQUARTERS);
            String tokenB = provider.issueAccessToken(20L, Role.ROLE_WAREHOUSE_MANAGER);

            AuthenticatedMember memberA = provider.extract(tokenA).orElseThrow();
            AuthenticatedMember memberB = provider.extract(tokenB).orElseThrow();

            assertThat(memberA.memberId()).isEqualTo(10L);
            assertThat(memberB.memberId()).isEqualTo(20L);
            assertThat(memberA.role()).isEqualTo("ROLE_HEADQUARTERS");
            assertThat(memberB.role()).isEqualTo("ROLE_WAREHOUSE_MANAGER");
        }
    }
}
