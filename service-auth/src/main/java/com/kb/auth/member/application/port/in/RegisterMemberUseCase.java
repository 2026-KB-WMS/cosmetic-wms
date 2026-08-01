package com.kb.auth.member.application.port.in;

import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.application.port.in.dto.RegisterMemberCommand;

public interface RegisterMemberUseCase {

    MemberResult register(RegisterMemberCommand command);
}
