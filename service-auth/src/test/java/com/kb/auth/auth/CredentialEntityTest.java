package com.kb.auth.auth;

import com.kb.auth.auth.domain.exception.CredentialValidationException;
import com.kb.auth.auth.domain.model.Credential;
import com.kb.auth.auth.domain.valueobject.LoginId;
import com.kb.auth.auth.fixture.CredentialTestBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CredentialEntityTest {

    @Nested
    class Credential_생성 {

        @Test
        void 올바른_값으로_Credential을_생성하면_credentialId는_null이다() {
            Credential credential = new CredentialTestBuilder().build();

            assertThat(credential.getCredentialId()).isNull();
            assertThat(credential.getMemberId()).isEqualTo(1L);
            assertThat(credential.getLoginId().value()).isEqualTo("user12345");
        }

        @Test
        void memberId가_null이면_예외를_던진다() {
            assertThatThrownBy(() -> new CredentialTestBuilder().memberId(null).build())
                    .isInstanceOf(CredentialValidationException.class);
        }

        @Test
        void 인코딩된_비밀번호가_null이면_예외를_던진다() {
            assertThatThrownBy(() -> new CredentialTestBuilder().encodedPassword(null).build())
                    .isInstanceOf(CredentialValidationException.class);
        }

        @Test
        void 인코딩된_비밀번호가_공백_문자열이면_예외를_던진다() {
            assertThatThrownBy(() -> new CredentialTestBuilder().encodedPassword("   ").build())
                    .isInstanceOf(CredentialValidationException.class);
        }
    }

    @Nested
    class LoginId_유효성_검증 {

        @Test
        void 유효한_길이의_loginId는_정상_생성된다() {
            assertThatNoException().isThrownBy(() -> new LoginId("valid_id"));
        }

        @Test
        void loginId가_null이면_예외를_던진다() {
            assertThatThrownBy(() -> new LoginId(null))
                    .isInstanceOf(CredentialValidationException.class);
        }

        @Test
        void loginId가_빈_문자열이면_예외를_던진다() {
            assertThatThrownBy(() -> new LoginId(""))
                    .isInstanceOf(CredentialValidationException.class);
        }

        @Test
        void loginId가_공백_문자열이면_예외를_던진다() {
            assertThatThrownBy(() -> new LoginId("     "))
                    .isInstanceOf(CredentialValidationException.class);
        }

        @Test
        void loginId가_최소_길이_미만이면_예외를_던진다() {
            assertThatThrownBy(() -> new LoginId("a".repeat(LoginId.MIN_LENGTH - 1)))
                    .isInstanceOf(CredentialValidationException.class);
        }

        @Test
        void loginId가_최대_길이를_초과하면_예외를_던진다() {
            assertThatThrownBy(() -> new LoginId("a".repeat(LoginId.MAX_LENGTH + 1)))
                    .isInstanceOf(CredentialValidationException.class);
        }

        @Test
        void loginId가_최소_길이와_같으면_정상_생성된다() {
            assertThatNoException().isThrownBy(() -> new LoginId("a".repeat(LoginId.MIN_LENGTH)));
        }

        @Test
        void loginId가_최대_길이와_같으면_정상_생성된다() {
            assertThatNoException().isThrownBy(() -> new LoginId("a".repeat(LoginId.MAX_LENGTH)));
        }
    }
}
