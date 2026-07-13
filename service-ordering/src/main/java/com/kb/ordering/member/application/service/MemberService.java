package com.kb.ordering.member.application.service;

import com.kb.ordering.member.application.port.in.FindMemberUseCase;
import com.kb.ordering.member.application.port.in.RegisterMemberUseCase;
import com.kb.ordering.member.application.port.in.dto.MemberResult;
import com.kb.ordering.member.application.port.in.dto.RegisterMemberCommand;
import com.kb.ordering.member.application.port.out.MemberPort;
import com.kb.ordering.member.domain.exception.DuplicateMemberException;
import com.kb.ordering.member.domain.exception.MemberNotFoundException;
import com.kb.ordering.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
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
