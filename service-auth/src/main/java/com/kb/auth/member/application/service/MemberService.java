package com.kb.auth.member.application.service;

import com.kb.auth.member.application.port.in.FindMemberUseCase;
import com.kb.auth.member.application.port.in.RegisterMemberUseCase;
import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.application.port.in.dto.RegisterMemberCommand;
import com.kb.auth.member.application.port.out.MemberPort;
import com.kb.auth.member.domain.exception.DuplicateMemberException;
import com.kb.auth.member.domain.exception.MemberNotFoundException;
import com.kb.auth.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService implements RegisterMemberUseCase, FindMemberUseCase {

    private final MemberPort memberPort;

    @Override
    public MemberResult findById(Long id) {
        Member member = memberPort.findById(id)
                .orElseThrow(MemberNotFoundException::new);

        return MemberResult.from(member);
    }

    @Override
    public Page<MemberResult> findAll(Pageable pageable) {
        return memberPort.findAll(pageable).map(MemberResult::from);
    }

    @Override
    @Transactional
    public MemberResult register(RegisterMemberCommand command) {
        if (memberPort.existsByEmail(command.email())) {
            throw new DuplicateMemberException();
        }

        Member member = Member.create(
                command.role(),
                command.memberName(),
                command.email(),
                command.phoneNumber()
        );

        return MemberResult.from(memberPort.save(member));
    }
}
