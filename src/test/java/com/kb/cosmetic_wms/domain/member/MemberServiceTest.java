package com.kb.cosmetic_wms.domain.member;

import com.kb.cosmetic_wms.domain.member.dto.MemberDetailResponseDto;
import com.kb.cosmetic_wms.domain.member.dto.MemberLoginRequestDto;
import com.kb.cosmetic_wms.domain.member.dto.MemberSignUpRequestDto;
import com.kb.cosmetic_wms.domain.member.entity.Member;
import com.kb.cosmetic_wms.domain.member.exception.DuplicateMemberException;
import com.kb.cosmetic_wms.domain.member.exception.LoginFailedException;
import com.kb.cosmetic_wms.domain.member.exception.MemberNotFoundException;
import com.kb.cosmetic_wms.domain.member.fixture.MemberDtoBuilder;
import com.kb.cosmetic_wms.domain.member.fixture.MemberTestBuilder;
import com.kb.cosmetic_wms.domain.member.repository.MemberRepository;
import com.kb.cosmetic_wms.domain.member.service.MemberService;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void 존재하는_ID를_조회하면_회원_정보를_반환한다() {
        // given
        Long mockMemberId = 1L;
        Member member = new MemberTestBuilder().build();
        ReflectionTestUtils.setField(member, "id", mockMemberId);

        given(memberRepository.findById(mockMemberId)).willReturn(Optional.of(member));

        // when
        MemberDetailResponseDto responseDto = memberService.findById(mockMemberId);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.memberName()).isEqualTo("홍길동");
    }

    @Test
    void 존재하지_않는_ID를_조회하면_MemberNotFoundException_예외를_던진다() {
        // given
        Long wrongMemberId = 999L;

        given(memberRepository.findById(wrongMemberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.findById(wrongMemberId))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 새로운_회원_정보를_입력하면_회원_등록에_성공한다() {
        // given
        MemberSignUpRequestDto requestDto = MemberDtoBuilder.signUpRequest().build();

        Member mockMember = new MemberTestBuilder()
                .loginId(requestDto.loginId())
                .role(requestDto.role())
                .build();
        ReflectionTestUtils.setField(mockMember, "id", 1L);

        given(memberRepository.existsByLoginId(requestDto.loginId())).willReturn(false);
        given(memberRepository.save(any(Member.class))).willReturn(mockMember);

        // when
        MemberDetailResponseDto responseDto = memberService.register(requestDto);

        // then
        assertThat(responseDto.id()).isEqualTo(1L);
    }

    @Test
    void 이미_존재하는_로그인_ID로_회원_등록을_요청하면_DuplicateMemberException_예외를_던진다() {
        // given
        MemberSignUpRequestDto requestDto = MemberDtoBuilder.signUpRequest().build();

        given(memberRepository.existsByLoginId(requestDto.loginId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.register(requestDto))
                .isInstanceOf(DuplicateMemberException.class);
    }

    @Test
    void 올바른_로그인_ID와_비밀번호를_입력하면_로그인에_성공하여_회원_정보를_반환한다() {
        // given
        MemberLoginRequestDto loginRequestDto = MemberDtoBuilder.loginRequest().build();

        Member existingMember = new MemberTestBuilder()
                .loginId(loginRequestDto.loginId())
                .password(loginRequestDto.password())
                .build();
        ReflectionTestUtils.setField(existingMember, "id", 1L);

        given(memberRepository.findByLoginId(loginRequestDto.loginId())).willReturn(Optional.of(existingMember));

        // when
        MemberDetailResponseDto responseDto = memberService.login(loginRequestDto);

        // then
        assertThat(responseDto.id()).isEqualTo(1L);
        assertThat(responseDto.memberName()).isEqualTo(existingMember.getMemberName());
    }

    @Test
    void 올바른_로그인_ID를_입력해도_비밀번호가_일치하지_않으면_LoginFailedException_예외를_던진다() {
        // given
        MemberLoginRequestDto loginRequestDto = MemberDtoBuilder.loginRequest()
                .password("password20934!")
                .build();

        Member existingMember = new MemberTestBuilder()
                .loginId(loginRequestDto.loginId())
                .password("password123!@")
                .build();

        given(memberRepository.findByLoginId(
                loginRequestDto.loginId())).willReturn(Optional.of(existingMember));

        // when & then
        assertThatThrownBy(() -> memberService.login(loginRequestDto))
                .isInstanceOf(LoginFailedException.class);
    }

}
