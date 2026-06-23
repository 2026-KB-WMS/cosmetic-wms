package com.kb.cosmetic_wms.member.application.service;

import com.kb.cosmetic_wms.member.application.port.in.*;
import com.kb.cosmetic_wms.member.application.port.out.MemberPort;
import com.kb.cosmetic_wms.member.domain.exception.DuplicateMemberException;
import com.kb.cosmetic_wms.member.domain.exception.LoginFailedException;
import com.kb.cosmetic_wms.member.domain.exception.MemberNotFoundException;
import com.kb.cosmetic_wms.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService implements RegisterMemberUseCase, LoginMemberUseCase, FindMemberUseCase {

    private final MemberPort memberPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberResult findById(Long id) {
        Member member = memberPort.findById(id)
                .orElseThrow(MemberNotFoundException::new);

        return MemberResult.from(member);
    }

    @Override
    @Transactional
    public MemberResult register(RegisterMemberCommand command) {
        if (memberPort.existsByLoginId(command.loginId())) {
            throw new DuplicateMemberException();
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        Member member = Member.create(
                command.loginId(),
                encodedPassword,
                command.role(),
                command.memberName(),
                command.email(),
                command.phoneNumber()
        );

        return MemberResult.from(memberPort.save(member));
    }

    @Override
    public MemberResult login(LoginMemberCommand command) {
        Member member = memberPort.findByLoginId(command.loginId())
                .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(command.password(), member.getEncodedPassword())) {
            throw new LoginFailedException();
        }

        return MemberResult.from(member);
    }
}