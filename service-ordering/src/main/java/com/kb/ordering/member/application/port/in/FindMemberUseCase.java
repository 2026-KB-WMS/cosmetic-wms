package com.kb.ordering.member.application.port.in;

import com.kb.ordering.member.application.port.in.dto.MemberResult;

public interface FindMemberUseCase {

    MemberResult findById(Long id);
}