package com.kb.auth.member;

import com.kb.auth.member.domain.exception.InvalidEmailException;
import com.kb.auth.member.domain.exception.InvalidPhoneNumberException;
import com.kb.auth.member.domain.exception.MemberValidationException;
import com.kb.auth.member.domain.model.Member;
import com.kb.auth.member.domain.model.Role;
import com.kb.auth.member.domain.valueobject.Email;
import com.kb.auth.member.domain.valueobject.PhoneNumber;
import com.kb.auth.member.fixture.MemberTestBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MemberEntityTest {

    @Nested
    class Member_생성 {

        @Test
        void 올바른_값으로_Member를_생성하면_memberId는_null이다() {
            Member member = new MemberTestBuilder().build();

            assertThat(member.getMemberId()).isNull();
            assertThat(member.getRole()).isEqualTo(Role.ROLE_HEADQUARTERS);
            assertThat(member.getMemberName()).isEqualTo("홍길동");
            assertThat(member.getEmail().value()).isEqualTo("test@example.com");
            assertThat(member.getPhoneNumber().value()).isEqualTo("010-1234-5678");
        }

        @Test
        void memberName이_null이면_예외를_던진다() {
            assertThatThrownBy(() -> new MemberTestBuilder().memberName(null).build())
                    .isInstanceOf(MemberValidationException.class);
        }

        @Test
        void memberName이_공백_문자열이면_예외를_던진다() {
            assertThatThrownBy(() -> new MemberTestBuilder().memberName("   ").build())
                    .isInstanceOf(MemberValidationException.class);
        }
    }

    @Nested
    class Email_유효성_검증 {

        @Test
        void 올바른_이메일_형식이면_정상_생성된다() {
            assertThatNoException().isThrownBy(() -> new Email("user@example.com"));
        }

        @Test
        void 서브도메인이_포함된_이메일도_정상_생성된다() {
            assertThatNoException().isThrownBy(() -> new Email("test.email@domain.co.kr"));
        }

        @Test
        void 이메일이_null이면_예외를_던진다() {
            assertThatThrownBy(() -> new Email(null))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        void at_기호가_없으면_예외를_던진다() {
            assertThatThrownBy(() -> new Email("invalidemail.com"))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        void 도메인이_없으면_예외를_던진다() {
            assertThatThrownBy(() -> new Email("user@"))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        void TLD가_없으면_예외를_던진다() {
            assertThatThrownBy(() -> new Email("user@example"))
                    .isInstanceOf(InvalidEmailException.class);
        }
    }

    @Nested
    class PhoneNumber_유효성_검증 {

        @Test
        void 올바른_휴대폰_번호_형식이면_정상_생성된다() {
            assertThatNoException().isThrownBy(() -> new PhoneNumber("010-1234-5678"));
        }

        @Test
        void 올바른_지역_번호_형식이면_정상_생성된다() {
            assertThatNoException().isThrownBy(() -> new PhoneNumber("02-123-4567"));
        }

        @Test
        void 전화번호가_null이면_예외를_던진다() {
            assertThatThrownBy(() -> new PhoneNumber(null))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }

        @Test
        void 하이픈이_없으면_예외를_던진다() {
            assertThatThrownBy(() -> new PhoneNumber("01012345678"))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }

        @Test
        void 자릿수가_부족하면_예외를_던진다() {
            assertThatThrownBy(() -> new PhoneNumber("010-123-456"))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }

        @Test
        void 유효하지_않은_국번이면_예외를_던진다() {
            // 012는 01[016789] 패턴에 해당하지 않는 유효하지 않은 국번
            assertThatThrownBy(() -> new PhoneNumber("012-1234-5678"))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }
    }
}
