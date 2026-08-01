package com.kb.auth.auth.adapter.out.member;

import com.kb.auth.auth.application.port.out.MemberPort;
import com.kb.auth.auth.application.port.out.dto.MemberInfo;
import com.kb.auth.auth.application.port.out.dto.MemberRegistration;
import com.kb.auth.member.adapter.in.internal.MemberInternalAdapter;
import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.application.port.in.dto.RegisterMemberCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberInternalClient implements MemberPort {

    private final MemberInternalAdapter memberInternalAdapter;

    @Override
    public MemberInfo loadById(Long memberId) {
        return toMemberInfo(memberInternalAdapter.findById(memberId));
    }

    @Override
    public MemberInfo register(MemberRegistration registration) {
        return toMemberInfo(memberInternalAdapter.register(new RegisterMemberCommand(
                registration.role(),
                registration.memberName(),
                registration.email(),
                registration.phoneNumber()
        )));
    }

    private MemberInfo toMemberInfo(MemberResult result) {
        return new MemberInfo(
                result.memberId(),
                result.memberName(),
                result.email(),
                result.phoneNumber(),
                result.role()
        );
    }
}