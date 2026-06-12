package com.kb.cosmetic_wms.domain.member.service;

import com.kb.cosmetic_wms.domain.member.dto.MemberDetailResponseDto;
import com.kb.cosmetic_wms.domain.member.dto.MemberLoginRequestDto;
import com.kb.cosmetic_wms.domain.member.dto.MemberSignUpRequestDto;
import com.kb.cosmetic_wms.domain.member.entity.Member;
import com.kb.cosmetic_wms.domain.member.exception.DuplicateMemberException;
import com.kb.cosmetic_wms.domain.member.exception.LoginFailedException;
import com.kb.cosmetic_wms.domain.member.exception.MemberNotFoundException;
import com.kb.cosmetic_wms.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private MemberRepository memberRepository;

    public MemberDetailResponseDto findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(MemberNotFoundException::new);

        return MemberDetailResponseDto.from(member);
    }

    @Transactional
    public MemberDetailResponseDto register(MemberSignUpRequestDto requestDto) {
        if (memberRepository.existsByLoginId(requestDto.loginId())) {
            throw new DuplicateMemberException();
        }
        Member member = toEntity(requestDto);
        Member savedMember = memberRepository.save(member);

        return MemberDetailResponseDto.from(savedMember);
    }

    public MemberDetailResponseDto login(MemberLoginRequestDto requestDto) {
        Member member = memberRepository.findByLoginId(requestDto.loginId())
                .orElseThrow(LoginFailedException::new);

        if (!member.getPassword().equals(requestDto.password())) {
            throw new LoginFailedException();
        }

        return MemberDetailResponseDto.from(member);
    }


    private Member toEntity(MemberSignUpRequestDto requestDto) {
        return Member.create(
                requestDto.loginId(),
                requestDto.password(),
                requestDto.role(),
                requestDto.memberName(),
                requestDto.email(),
                requestDto.phoneNumber()
        );
    }
}
