package com.kb.ordering.auth;

import com.kb.ordering.auth.domain.exception.AuthErrorCode;
import com.kb.ordering.auth.domain.exception.CredentialValidationException;
import com.kb.ordering.auth.domain.model.Credential;
import com.kb.ordering.auth.fixture.CredentialTestBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CredentialEntityTest {

    @Test
    void 올바른_입력값으로_생성_시_정상적으로_도메인_객체가_생성된다() {
        // given & when
        Credential credential = new CredentialTestBuilder().build();

        // then
        Assertions.assertThat(credential.getMemberId()).isEqualTo(1L);
        Assertions.assertThat(credential.getLoginId().value()).isEqualTo("admin01");
    }

    @Test
    void 회원_ID_없이_생성하면_예외를_던진다() {
        Assertions.assertThatThrownBy(() ->
                        new CredentialTestBuilder()
                                .memberId(null)
                                .build()
                )
                .isInstanceOf(CredentialValidationException.class)
                .hasMessage(AuthErrorCode.INVALID_CREDENTIAL.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    "})
    void 로그인_ID가_비어있거나_공백이면_예외를_던진다(String emptyLoginId) {
        Assertions.assertThatThrownBy(() ->
                        new CredentialTestBuilder()
                                .loginId(emptyLoginId)
                                .build()
                )
                .isInstanceOf(CredentialValidationException.class)
                .hasMessage(AuthErrorCode.INVALID_CREDENTIAL.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"adminLoginIdThatItsLengthMoreThanFifty12345667890112423", "id"})
    void 로그인_ID가_5자_이상_50자_이하가_아니면_예외를_던진다(String invalidLengthLoginId) {
        Assertions.assertThatThrownBy(() ->
                        new CredentialTestBuilder()
                                .loginId(invalidLengthLoginId)
                                .build()
                )
                .isInstanceOf(CredentialValidationException.class)
                .hasMessage(AuthErrorCode.INVALID_CREDENTIAL.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void 비밀번호가_비어있거나_공백이면_예외를_던진다(String emptyPassword) {
        Assertions.assertThatThrownBy(() ->
                        new CredentialTestBuilder()
                                .encodedPassword(emptyPassword)
                                .build()
                )
                .isInstanceOf(CredentialValidationException.class)
                .hasMessage(AuthErrorCode.INVALID_CREDENTIAL.getMessage());
    }
}
