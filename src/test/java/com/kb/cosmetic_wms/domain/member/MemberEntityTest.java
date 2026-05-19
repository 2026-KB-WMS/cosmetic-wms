package com.kb.cosmetic_wms.domain.member;

import com.kb.cosmetic_wms.domain.member.dto.MemberSignUpRequest;
import com.kb.cosmetic_wms.domain.member.entity.Member;
import com.kb.cosmetic_wms.domain.member.enums.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MemberEntityTest {

    @Test
    void 올바른_입력값으로_가입_시_정상적으로_엔티티가_생성된다() {
        // given
        MemberSignUpRequest request = new MemberSignUpRequest(
                "admin01",
                "password123!",
                Role.ROLE_HQ,
                "홍길동",
                "admin@example.com",
                "010-1234-5678"
        );

        // when
        Member member = Member.create(request);

        // then
        Assertions.assertThat(member.getLoginId()).isEqualTo("admin01");
        Assertions.assertThat(member.getRole()).isEqualTo(Role.ROLE_HQ);
        Assertions.assertThat(member.getMemberName()).isEqualTo("홍길동");
    }

    @ParameterizedTest
    @ValueSource(strings = {"adminexample.com", "admin@example", "admin@", "@example.com", "^#$a@1436&*.com"})
    void 잘못된_형식의_이메일을_입력했을_때_예외를_던진다(String invalidEmail) {
        // given
        MemberSignUpRequest request = new MemberSignUpRequest(
                "admin01",
                "password123!",
                Role.ROLE_HQ,
                "홍길동",
                invalidEmail,
                "010-1234-5678"
        );

        // when & then
        Assertions.assertThatThrownBy(() -> Member.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("올바르지 않은 이메일 형식입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"133245", "01012345678", "02-123-123", "abc-defg-hijk"})
    void 잘못된_형식의_전화번호를_입력했을_때_예외를_던진다(String invalidPhoneNumber) {
        // given
        MemberSignUpRequest request = new MemberSignUpRequest(
                "admin01",
                "password123!",
                Role.ROLE_HQ,
                "홍길동",
                "admin@example.com",
                invalidPhoneNumber
        );

        // when & then
        Assertions.assertThatThrownBy(() -> Member.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("올바르지 않은 전화번호 형식입니다. (예: 010-1234-5678)");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    "})
    void 로그인_ID가_비어있거나_공백이면_예외를_던진다(String emptyLoginId) {
        // given
        MemberSignUpRequest request = new MemberSignUpRequest(
                emptyLoginId,
                "password123!",
                Role.ROLE_HQ,
                "홍길동",
                "admin@example.com",
                "010-1234-5678"
        );

        // when & then
        Assertions.assertThatThrownBy(() -> Member.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("로그인 ID는 필수 입력 항목입니다.");
    }
}
