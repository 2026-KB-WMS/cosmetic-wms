package com.kb.cosmetic_wms.domain.member;

import com.kb.cosmetic_wms.domain.member.fixture.MemberDtoBuilder;
import com.kb.cosmetic_wms.domain.member.fixture.MemberTestBuilder;
import com.kb.cosmetic_wms.member.adapter.in.web.MemberLoginRequest;
import com.kb.cosmetic_wms.member.adapter.in.web.MemberSignUpRequest;
import com.kb.cosmetic_wms.member.application.port.in.MemberResult;
import com.kb.cosmetic_wms.member.application.port.out.MemberPort;
import com.kb.cosmetic_wms.member.application.service.MemberService;
import com.kb.cosmetic_wms.member.domain.exception.DuplicateMemberException;
import com.kb.cosmetic_wms.member.domain.exception.LoginFailedException;
import com.kb.cosmetic_wms.member.domain.exception.MemberNotFoundException;
import com.kb.cosmetic_wms.member.domain.model.Member;
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
    void 이미_존재하는_로그인_ID로_회원_등록을_요청하면_DuplicateMemberException_예외를_던진다() {
        // given
        MemberSignUpRequest request = MemberDtoBuilder.signUpRequest().build();

        given(memberPort.existsByLoginId(request.loginId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.register(request.toCommand()))
                .isInstanceOf(DuplicateMemberException.class);
    }

    @Test
    void 올바른_로그인_ID와_비밀번호를_입력하면_로그인에_성공하여_회원_정보를_반환한다() {
        // given
        MemberLoginRequest request = MemberDtoBuilder.loginRequest().build();
        String encodedPassword = passwordEncoder.encode(request.password());

        Member existingMember = new MemberTestBuilder()
                .loginId(request.loginId())
                .password(encodedPassword)
                .buildWithId(1L);

        given(memberPort.findByLoginId(request.loginId())).willReturn(Optional.of(existingMember));

        // when
        MemberResult result = memberService.login(request.toCommand());

        // then
        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.memberName()).isEqualTo(existingMember.getMemberName());
    }

    @Test
    void 올바른_로그인_ID를_입력해도_비밀번호가_일치하지_않으면_LoginFailedException_예외를_던진다() {
        // given
        MemberLoginRequest request = MemberDtoBuilder.loginRequest()
                .password("password20934!")
                .build();

        Member existingMember = new MemberTestBuilder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode("password123!@"))
                .buildWithId(1L);

        given(memberPort.findByLoginId(request.loginId())).willReturn(Optional.of(existingMember));

        // when & then
        assertThatThrownBy(() -> memberService.login(request.toCommand()))
                .isInstanceOf(LoginFailedException.class);
    }
}