package com.kb.auth.member.application.port.in;

import com.kb.auth.member.application.port.in.dto.MemberResult;

public interface FindMemberUseCase {

    MemberResult findById(Long id);
}