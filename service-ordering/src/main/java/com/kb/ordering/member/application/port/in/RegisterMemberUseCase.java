package com.kb.ordering.member.application.port.in;

import com.kb.ordering.member.application.port.in.dto.MemberResult;
import com.kb.ordering.member.application.port.in.dto.RegisterMemberCommand;

public interface RegisterMemberUseCase {

    MemberResult register(RegisterMemberCommand command);
}
