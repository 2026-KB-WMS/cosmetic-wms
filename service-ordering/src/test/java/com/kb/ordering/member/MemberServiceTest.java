package com.kb.ordering.member;

import com.kb.ordering.member.application.port.in.dto.MemberResult;
import com.kb.ordering.member.application.port.in.dto.RegisterMemberCommand;
import com.kb.ordering.member.application.port.out.MemberPort;
import com.kb.ordering.member.application.service.MemberService;
import com.kb.ordering.member.domain.exception.DuplicateMemberException;
import com.kb.ordering.member.domain.exception.MemberNotFoundException;
import com.kb.ordering.member.domain.model.Member;
import com.kb.ordering.member.domain.model.Role;
import com.kb.ordering.member.fixture.MemberTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberPort memberPort;

    @Test
    void 존재하는_ID를_조회하면_회원_정보를_반환한다() {
        // given
        Long memberId = 1L;
        Member member = new MemberTestBuilder().buildWithId(memberId);

        given(memberPort.findById(memberId)).willReturn(Optional.of(member));

        // when
        MemberResult result = memberService.findById(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.memberName()).isEqualTo("홍길동");
    }

    @Test
    void 존재하지_않는_ID를_조회하면_MemberNotFoundException_예외를_던진다() {
        // given
        Long wrongMemberId = 999L;

        given(memberPort.findById(wrongMemberId)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> memberService.findById(wrongMemberId))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 새로운_회원_정보를_입력하면_회원_등록에_성공한다() {
        // given
        RegisterMemberCommand command = new RegisterMemberCommand(
                Role.ROLE_HEADQUARTERS, "홍길동", "admin@example.com", "010-1234-5678"
        );
        Member savedMember = new MemberTestBuilder().buildWithId(1L);

        given(memberPort.existsByEmail(command.email())).willReturn(false);
        given(memberPort.save(any(Member.class))).willReturn(savedMember);

        // when
        MemberResult result = memberService.register(command);

        // then
        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("admin@example.com");
    }

    @Test
    void 이미_등록된_이메일로_회원_등록을_요청하면_DuplicateMemberException_예외를_던진다() {
        // given
        RegisterMemberCommand command = new RegisterMemberCommand(
                Role.ROLE_HEADQUARTERS, "홍길동", "admin@example.com", "010-1234-5678"
        );

        given(memberPort.existsByEmail(command.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.register(command))
                .isInstanceOf(DuplicateMemberException.class);
        then(memberPort).should(never()).save(any(Member.class));
    }
}
