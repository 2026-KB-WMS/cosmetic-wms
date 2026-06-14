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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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

        String encodedPassword = passwordEncoder.encode(requestDto.password());

        Member member = toEntity(requestDto, encodedPassword);
        Member savedMember = memberRepository.save(member);

        return MemberDetailResponseDto.from(savedMember);
    }

    public MemberDetailResponseDto login(MemberLoginRequestDto requestDto) {
        Member member = memberRepository.findByLoginId(requestDto.loginId())
                .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(requestDto.password(), member.getPassword())) {
            throw new LoginFailedException();
        }

        return MemberDetailResponseDto.from(member);
    }

    private Member toEntity(MemberSignUpRequestDto requestDto, String encodedPassword) {
        return Member.create(
                requestDto.loginId(),
                encodedPassword,
                requestDto.role(),
                requestDto.memberName(),
                requestDto.email(),
                requestDto.phoneNumber()
        );
    }
}
