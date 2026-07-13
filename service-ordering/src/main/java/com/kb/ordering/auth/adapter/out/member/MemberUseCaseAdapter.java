package com.kb.ordering.auth.adapter.out.member;

import com.kb.ordering.auth.application.port.out.LoadMemberPort;
import com.kb.ordering.auth.application.port.out.RegisterMemberPort;
import com.kb.ordering.auth.application.port.out.dto.MemberInfo;
import com.kb.ordering.auth.application.port.out.dto.MemberRegistration;
import com.kb.ordering.member.application.port.in.FindMemberUseCase;
import com.kb.ordering.member.application.port.in.RegisterMemberUseCase;
import com.kb.ordering.member.application.port.in.dto.MemberResult;
import com.kb.ordering.member.application.port.in.dto.RegisterMemberCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUseCaseAdapter implements RegisterMemberPort, LoadMemberPort {

    private final RegisterMemberUseCase registerMemberUseCase;
    private final FindMemberUseCase findMemberUseCase;

    @Override
    public MemberInfo register(MemberRegistration registration) {
        MemberResult result = registerMemberUseCase.register(new RegisterMemberCommand(
                registration.role(),
                registration.memberName(),
                registration.email(),
                registration.phoneNumber()
        ));
        return toMemberInfo(result);
    }

    @Override
    public MemberInfo loadById(Long memberId) {
        return toMemberInfo(findMemberUseCase.findById(memberId));
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
