package com.kb.ordering.member;

import com.kb.ordering.member.domain.exception.InvalidEmailException;
import com.kb.ordering.member.domain.exception.InvalidPhoneNumberException;
import com.kb.ordering.member.domain.exception.MemberErrorCode;
import com.kb.ordering.member.domain.exception.MemberValidationException;
import com.kb.ordering.member.domain.model.Member;
import com.kb.ordering.member.domain.model.Role;
import com.kb.ordering.member.fixture.MemberTestBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MemberEntityTest {

    @Test
    void 올바른_입력값으로_생성_시_정상적으로_도메인_객체가_생성된다() {
        // given & when
        Member member = new MemberTestBuilder().build();

        // then
        Assertions.assertThat(member.getRole()).isEqualTo(Role.ROLE_HEADQUARTERS);
        Assertions.assertThat(member.getMemberName()).isEqualTo("홍길동");
        Assertions.assertThat(member.getEmail().value()).isEqualTo("admin@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"adminexample.com", "admin@example", "admin@", "@example.com", "^#$a@1436&*.com"})
    void 잘못된_형식의_이메일을_입력했을_때_예외를_던진다(String invalidEmail) {
        Assertions.assertThatThrownBy(() ->
                        new MemberTestBuilder()
                                .email(invalidEmail)
                                .build()
                )
                .isInstanceOf(InvalidEmailException.class)
                .hasMessage(MemberErrorCode.INVALID_EMAIL.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"133245", "01012345678", "02-123-123", "abc-defg-hijk"})
    void 잘못된_형식의_전화번호를_입력했을_때_예외를_던진다(String invalidPhoneNumber) {
        Assertions.assertThatThrownBy(() ->
                        new MemberTestBuilder()
                                .phoneNumber(invalidPhoneNumber)
                                .build()
                )
                .isInstanceOf(InvalidPhoneNumberException.class)
                .hasMessage(MemberErrorCode.INVALID_PHONE_NUMBER.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    "})
    void 이름이_비어있거나_공백이면_예외를_던진다(String blankName) {
        Assertions.assertThatThrownBy(() ->
                        new MemberTestBuilder()
                                .memberName(blankName)
                                .build()
                )
                .isInstanceOf(MemberValidationException.class)
                .hasMessage(MemberErrorCode.INVALID_MEMBER_VALIDATION.getMessage());
    }
}
