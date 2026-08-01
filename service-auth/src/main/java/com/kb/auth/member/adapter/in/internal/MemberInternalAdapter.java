package com.kb.auth.member.adapter.in.internal;

import com.kb.auth.member.application.port.in.FindMemberUseCase;
import com.kb.auth.member.application.port.in.RegisterMemberUseCase;
import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.application.port.in.dto.RegisterMemberCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberInternalAdapter {

    private final FindMemberUseCase findMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;

    public MemberResult findById(Long id) {
        return findMemberUseCase.findById(id);
    }

    public MemberResult register(RegisterMemberCommand command) {
        return registerMemberUseCase.register(command);
    }
}