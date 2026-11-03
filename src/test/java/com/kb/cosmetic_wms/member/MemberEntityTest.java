package com.kb.cosmetic_wms.member;

import com.kb.cosmetic_wms.member.fixture.MemberTestBuilder;
import com.kb.cosmetic_wms.member.domain.exception.InvalidEmailException;
import com.kb.cosmetic_wms.member.domain.exception.InvalidPhoneNumberException;
import com.kb.cosmetic_wms.member.domain.exception.MemberErrorCode;
import com.kb.cosmetic_wms.member.domain.exception.MemberValidationException;
import com.kb.cosmetic_wms.member.domain.model.Member;
import com.kb.cosmetic_wms.member.domain.model.Role;
import com.kb.cosmetic_wms.member.domain.valueobject.LoginId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

public class MemberEntityTest {

    @Test
    void 올바른_입력값으로_가입_시_정상적으로_도메인_객체가_생성된다() {
        // given & when
        Member member = new MemberTestBuilder().build();

        // then
        Assertions.assertThat(member.getLoginId()).isEqualTo(new LoginId("admin01"));
        Assertions.assertThat(member.getRole()).isEqualTo(Role.ROLE_HEADQUARTERS);
        Assertions.assertThat(member.getMemberName()).isEqualTo("홍길동");
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
    void 로그인_ID가_비어있거나_공백이면_예외를_던진다(String emptyLoginId) {
        Assertions.assertThatThrownBy(() ->
                        new MemberTestBuilder()
                                .loginId(emptyLoginId)
                                .build()
                )
                .isInstanceOf(MemberValidationException.class)
                .hasMessage(MemberErrorCode.INVALID_MEMBER_VALIDATION.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"adminLoginIdThatItsLengthMoreThanFifty12345667890112423", "id"})
    void 로그인_ID가_5자_이상_50자_이하가_아니면_예외를_던진다(String invalidLengthLoginId) {
        Assertions.assertThatThrownBy(() ->
                        new MemberTestBuilder()
                                .loginId(invalidLengthLoginId)
                                .build()
                )
                .isInstanceOf(MemberValidationException.class)
                .hasMessage(MemberErrorCode.INVALID_MEMBER_VALIDATION.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void 비밀번호가_비어있거나_공백이면_예외를_던진다(String emptyPassword) {
        Assertions.assertThatThrownBy(() ->
                        new MemberTestBuilder()
                                .password(emptyPassword)
                                .build()
                )
                .isInstanceOf(MemberValidationException.class)
                .hasMessage(MemberErrorCode.INVALID_MEMBER_VALIDATION.getMessage());
    }

    @Test
    void 본사_관리자_권한은_직접_가입할_수_없다() {
        Assertions.assertThat(Role.ROLE_HEADQUARTERS.isSelfRegistrable()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = "ROLE_HEADQUARTERS", mode = EnumSource.Mode.EXCLUDE)
    void 본사_관리자를_제외한_권한은_직접_가입할_수_있다(Role role) {
        Assertions.assertThat(role.isSelfRegistrable()).isTrue();
    }
}
