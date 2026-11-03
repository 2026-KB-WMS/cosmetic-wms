package com.kb.cosmetic_wms.member;

import com.kb.cosmetic_wms.member.domain.exception.InvalidPasswordException;
import com.kb.cosmetic_wms.member.domain.exception.MemberErrorCode;
import com.kb.cosmetic_wms.member.domain.valueobject.RawPassword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawPasswordTest {

    @ParameterizedTest
    @ValueSource(strings = {"password1!", "Abcdef1@", "a1!aaaaa", "한글이섞여도Pass1!"})
    void 영문_숫자_특수문자를_포함한_8자_이상_비밀번호는_생성된다(String valid) {
        assertThat(new RawPassword(valid).value()).isEqualTo(valid);
    }

    @Test
    void 정확히_50자인_비밀번호는_생성된다() {
        String fifty = "a1!" + "x".repeat(47);

        assertThat(new RawPassword(fifty).value()).hasSize(50);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "a1!aaaa", "password!!", "12345678!", "password12"})
    void 규칙을_만족하지_않는_비밀번호는_예외를_던진다(String invalid) {
        assertThatThrownBy(() -> new RawPassword(invalid))
                .isInstanceOf(InvalidPasswordException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_PASSWORD);
    }

    @Test
    void 비밀번호가_50자를_넘으면_예외를_던진다() {
        String fiftyOne = "a1!" + "x".repeat(48);

        assertThatThrownBy(() -> new RawPassword(fiftyOne))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void toString은_비밀번호를_노출하지_않는다() {
        RawPassword password = new RawPassword("password1!");

        assertThat(password.toString()).doesNotContain("password1!");
    }
}
