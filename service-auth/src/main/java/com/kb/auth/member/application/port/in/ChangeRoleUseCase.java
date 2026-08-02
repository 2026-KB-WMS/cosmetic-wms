package com.kb.auth.member.application.port.in;

import com.kb.auth.member.application.port.in.dto.ChangeRoleCommand;
import com.kb.auth.member.application.port.in.dto.MemberResult;

public interface ChangeRoleUseCase {

    MemberResult changeRole(ChangeRoleCommand command);
}
