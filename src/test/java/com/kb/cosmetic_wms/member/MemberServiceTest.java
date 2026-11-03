package com.kb.cosmetic_wms.member;

import com.kb.cosmetic_wms.member.fixture.MemberDtoBuilder;
import com.kb.cosmetic_wms.member.fixture.MemberTestBuilder;
import com.kb.cosmetic_wms.member.application.port.in.LoginMemberCommand;
import com.kb.cosmetic_wms.member.application.port.in.MemberIdentityResult;
import com.kb.cosmetic_wms.member.adapter.in.web.MemberSignUpRequest;
import com.kb.cosmetic_wms.member.application.port.in.MemberResult;
import com.kb.cosmetic_wms.member.application.port.out.MemberPort;
import com.kb.cosmetic_wms.member.application.service.MemberService;
import com.kb.cosmetic_wms.member.domain.exception.DuplicateMemberException;
import com.kb.cosmetic_wms.member.domain.exception.InvalidPasswordException;
import com.kb.cosmetic_wms.member.domain.exception.LoginFailedException;
import com.kb.cosmetic_wms.member.domain.exception.MemberNotFoundException;
import com.kb.cosmetic_wms.member.domain.exception.RoleNotAllowedForSignUpException;
import com.kb.cosmetic_wms.member.domain.model.Member;
import com.kb.cosmetic_wms.member.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberPort memberPort;

    @Spy
    private BCryptPasswordEncoder passwordEncoder;

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

        // when & then
        assertThatThrownBy(() -> memberService.findById(wrongMemberId))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 새로운_회원_정보를_입력하면_회원_등록에_성공한다() {
        // given
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest().build();
        Member savedMember = new MemberTestBuilder()
                .loginId(request.loginId())
                .role(request.role())
                .buildWithId(1L);

        given(memberPort.existsByLoginId(request.loginId())).willReturn(false);
        given(memberPort.save(any(Member.class))).willReturn(savedMember);

        // when
        MemberResult result = memberService.register(request.toCommand());

        // then
        assertThat(result.memberId()).isEqualTo(1L);
    }

    @Test
    void 본사_관리자_권한으로_회원_등록을_요청하면_RoleNotAllowedForSignUpException_예외를_던지고_저장하지_않는다() {
        // given
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest()
                .role(Role.ROLE_HEADQUARTERS)
                .build();

        // when & then
        assertThatThrownBy(() -> memberService.register(request.toCommand()))
                .isInstanceOf(RoleNotAllowedForSignUpException.class);
        verify(memberPort, never()).save(any(Member.class));
    }

    @Test
    void 규칙에_맞지_않는_비밀번호로_회원_등록을_요청하면_InvalidPasswordException_예외를_던지고_저장하지_않는다() {
        // given
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest()
                .password("weak")
                .build();

        // when & then
        assertThatThrownBy(() -> memberService.register(request.toCommand()))
                .isInstanceOf(InvalidPasswordException.class);
        verify(memberPort, never()).save(any(Member.class));
    }

    @Test
    void 이미_존재하는_로그인_ID로_회원_등록을_요청하면_DuplicateMemberException_예외를_던진다() {
        // given
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest().build();

        given(memberPort.existsByLoginId(request.loginId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.register(request.toCommand()))
                .isInstanceOf(DuplicateMemberException.class);
    }

    @Test
    void 올바른_로그인_ID와_비밀번호를_입력하면_인증에_성공하여_회원_ID와_권한을_반환한다() {
        // given
        LoginMemberCommand command = new LoginMemberCommand("admin01", "password123!");
        Member existingMember = new MemberTestBuilder()
                .loginId(command.loginId())
                .password(passwordEncoder.encode(command.password()))
                .role(Role.ROLE_WAREHOUSE_MANAGER)
                .buildWithId(1L);

        given(memberPort.findByLoginId(command.loginId())).willReturn(Optional.of(existingMember));

        // when
        MemberIdentityResult result = memberService.authenticate(command);

        // then
        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.role()).isEqualTo("ROLE_WAREHOUSE_MANAGER");
    }

    @Test
    void 비밀번호가_일치하지_않으면_LoginFailedException_예외를_던진다() {
        // given
        LoginMemberCommand command = new LoginMemberCommand("admin01", "wrong-password1!");
        Member existingMember = new MemberTestBuilder()
                .loginId(command.loginId())
                .password(passwordEncoder.encode("password123!"))
                .buildWithId(1L);

        given(memberPort.findByLoginId(command.loginId())).willReturn(Optional.of(existingMember));

        // when & then
        assertThatThrownBy(() -> memberService.authenticate(command))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    void 존재하지_않는_로그인_ID로_인증하면_LoginFailedException_예외를_던진다() {
        // given
        LoginMemberCommand command = new LoginMemberCommand("nobody01", "password123!");
        given(memberPort.findByLoginId(command.loginId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.authenticate(command))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    void 회원_ID로_식별_정보를_조회하면_회원_ID와_권한을_반환한다() {
        // given
        Member existingMember = new MemberTestBuilder().role(Role.ROLE_FRANCHISE_MANAGER).buildWithId(5L);
        given(memberPort.findById(5L)).willReturn(Optional.of(existingMember));

        // when
        Optional<MemberIdentityResult> result = memberService.findIdentity(5L);

        // then
        assertThat(result).contains(new MemberIdentityResult(5L, "ROLE_FRANCHISE_MANAGER"));
    }

    @Test
    void 존재하지_않는_회원_ID의_식별_정보를_조회하면_빈_결과를_반환한다() {
        // given
        given(memberPort.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThat(memberService.findIdentity(999L)).isEmpty();
    }
}
