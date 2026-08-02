package com.kb.auth.member;

import com.kb.auth.member.application.port.in.dto.ChangeRoleCommand;
import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.application.port.in.dto.RegisterMemberCommand;
import com.kb.auth.member.application.port.out.MemberPort;
import com.kb.auth.member.application.service.MemberService;
import com.kb.auth.member.domain.exception.DuplicateMemberException;
import com.kb.auth.member.domain.exception.DuplicatePhoneNumberException;
import com.kb.auth.member.domain.exception.MemberNotFoundException;
import com.kb.auth.member.domain.model.Member;
import com.kb.auth.member.domain.model.Role;
import com.kb.auth.member.fixture.MemberTestBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberPort memberPort;

    @Nested
    class 회원_조회 {

        @Test
        void 존재하는_ID로_조회하면_MemberResult를_반환한다() {
            // given
            Member member = new MemberTestBuilder().build();
            ReflectionTestUtils.setField(member, "memberId", 1L);
            given(memberPort.findById(1L)).willReturn(Optional.of(member));

            // when
            MemberResult result = memberService.findById(1L);

            // then
            assertThat(result.memberId()).isEqualTo(1L);
            assertThat(result.memberName()).isEqualTo("홍길동");
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.role()).isEqualTo(Role.ROLE_HEADQUARTERS);
        }

        @Test
        void 존재하지_않는_ID로_조회하면_MemberNotFoundException을_던진다() {
            // given
            given(memberPort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.findById(999L))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @Nested
    class 회원_등록 {

        @Test
        void 올바른_정보로_등록하면_저장된_MemberResult를_반환한다() {
            // given
            RegisterMemberCommand command = new RegisterMemberCommand(
                    Role.ROLE_WAREHOUSE_MANAGER, "홍길동", "test@example.com", "010-1234-5678"
            );
            given(memberPort.existsByEmail("test@example.com")).willReturn(false);
            given(memberPort.existsByPhoneNumber("010-1234-5678")).willReturn(false);
            given(memberPort.save(any(Member.class))).willAnswer(inv -> {
                Member m = inv.getArgument(0);
                ReflectionTestUtils.setField(m, "memberId", 1L);
                return m;
            });

            // when
            MemberResult result = memberService.register(command);

            // then
            assertThat(result.memberId()).isEqualTo(1L);
            assertThat(result.memberName()).isEqualTo("홍길동");
            assertThat(result.email()).isEqualTo("test@example.com");
        }

        @Test
        void 이미_등록된_이메일로_등록하면_DuplicateMemberException을_던진다() {
            // given
            RegisterMemberCommand command = new RegisterMemberCommand(
                    Role.ROLE_WAREHOUSE_MANAGER, "홍길동", "test@example.com", "010-1234-5678"
            );
            given(memberPort.existsByEmail("test@example.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.register(command))
                    .isInstanceOf(DuplicateMemberException.class);
        }

        @Test
        void 이미_등록된_전화번호로_등록하면_DuplicatePhoneNumberException을_던진다() {
            // given
            RegisterMemberCommand command = new RegisterMemberCommand(
                    Role.ROLE_WAREHOUSE_MANAGER, "홍길동", "test@example.com", "010-1234-5678"
            );
            given(memberPort.existsByEmail("test@example.com")).willReturn(false);
            given(memberPort.existsByPhoneNumber("010-1234-5678")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.register(command))
                    .isInstanceOf(DuplicatePhoneNumberException.class);
        }

        @Test
        void 회원_등록_성공_시_Member가_정확히_1회_저장된다() {
            // given
            RegisterMemberCommand command = new RegisterMemberCommand(
                    Role.ROLE_WAREHOUSE_MANAGER, "홍길동", "test@example.com", "010-1234-5678"
            );
            given(memberPort.existsByEmail(anyString())).willReturn(false);
            given(memberPort.existsByPhoneNumber(anyString())).willReturn(false);
            given(memberPort.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            memberService.register(command);

            // then
            then(memberPort).should(times(1)).save(any(Member.class));
        }
    }

    @Nested
    class 역할_변경 {

        @Test
        void 존재하는_회원의_역할을_변경하면_변경된_MemberResult를_반환한다() {
            // given
            Member member = new MemberTestBuilder().role(Role.ROLE_WAREHOUSE_MANAGER).build();
            ReflectionTestUtils.setField(member, "memberId", 1L);
            given(memberPort.findById(1L)).willReturn(Optional.of(member));
            given(memberPort.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            MemberResult result = memberService.changeRole(new ChangeRoleCommand(1L, Role.ROLE_HEADQUARTERS));

            // then
            assertThat(result.role()).isEqualTo(Role.ROLE_HEADQUARTERS);
        }

        @Test
        void 존재하지_않는_회원의_역할을_변경하면_MemberNotFoundException을_던진다() {
            // given
            given(memberPort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.changeRole(new ChangeRoleCommand(999L, Role.ROLE_HEADQUARTERS)))
                    .isInstanceOf(MemberNotFoundException.class);
        }

        @Test
        void 역할_변경_성공_시_변경된_Member가_정확히_1회_저장된다() {
            // given
            Member member = new MemberTestBuilder().role(Role.ROLE_WAREHOUSE_MANAGER).build();
            ReflectionTestUtils.setField(member, "memberId", 1L);
            given(memberPort.findById(1L)).willReturn(Optional.of(member));
            given(memberPort.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            memberService.changeRole(new ChangeRoleCommand(1L, Role.ROLE_HEADQUARTERS));

            // then
            then(memberPort).should(times(1)).save(any(Member.class));
        }
    }
}
