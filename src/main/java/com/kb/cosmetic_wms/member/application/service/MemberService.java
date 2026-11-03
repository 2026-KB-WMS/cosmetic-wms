package com.kb.cosmetic_wms.member.application.service;

import com.kb.cosmetic_wms.member.application.port.in.*;
import com.kb.cosmetic_wms.member.application.port.out.MemberPort;
import com.kb.cosmetic_wms.member.domain.exception.DuplicateMemberException;
import com.kb.cosmetic_wms.member.domain.exception.LoginFailedException;
import com.kb.cosmetic_wms.member.domain.exception.MemberNotFoundException;
import com.kb.cosmetic_wms.member.domain.exception.RoleNotAllowedForSignUpException;
import com.kb.cosmetic_wms.member.domain.model.Member;
import com.kb.cosmetic_wms.member.domain.valueobject.RawPassword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService implements RegisterMemberUseCase, MemberIdentityUseCase, FindMemberUseCase {

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
        if (!command.role().isSelfRegistrable()) {
            throw new RoleNotAllowedForSignUpException();
        }

        RawPassword rawPassword = new RawPassword(command.password());

        if (memberPort.existsByLoginId(command.loginId())) {
            throw new DuplicateMemberException();
        }

        String encodedPassword = passwordEncoder.encode(rawPassword.value());

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
    public MemberIdentityResult authenticate(LoginMemberCommand command) {
        Member member = memberPort.findByLoginId(command.loginId())
                .orElseThrow(() -> {
                    log.warn("로그인에 실패했습니다. 존재하지 않는 로그인 ID입니다.");
                    return new LoginFailedException();
                });

        if (!passwordEncoder.matches(command.password(), member.getEncodedPassword())) {
            log.warn("로그인에 실패했습니다. 비밀번호가 일치하지 않습니다. memberId={}", member.getMemberId());
            throw new LoginFailedException();
        }

        return toIdentity(member);
    }

    @Override
    public Optional<MemberIdentityResult> findIdentity(Long memberId) {
        return memberPort.findById(memberId)
                .map(MemberService::toIdentity);
    }

    private static MemberIdentityResult toIdentity(Member member) {
        return new MemberIdentityResult(member.getMemberId(), member.getRole().name());
    }
}
