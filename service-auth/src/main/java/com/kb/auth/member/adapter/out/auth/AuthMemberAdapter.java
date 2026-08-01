package com.kb.auth.member.adapter.out.auth;

import com.kb.auth.auth.application.port.out.LoadMemberPort;
import com.kb.auth.auth.application.port.out.RegisterMemberPort;
import com.kb.auth.auth.application.port.out.dto.MemberInfo;
import com.kb.auth.auth.application.port.out.dto.MemberRegistration;
import com.kb.auth.member.application.port.in.FindMemberUseCase;
import com.kb.auth.member.application.port.in.RegisterMemberUseCase;
import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.application.port.in.dto.RegisterMemberCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthMemberAdapter implements LoadMemberPort, RegisterMemberPort {

    private final FindMemberUseCase findMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;

    @Override
    public MemberInfo loadById(Long memberId) {
        return toMemberInfo(findMemberUseCase.findById(memberId));
    }

    @Override
    public MemberInfo register(MemberRegistration registration) {
        return toMemberInfo(registerMemberUseCase.register(new RegisterMemberCommand(
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