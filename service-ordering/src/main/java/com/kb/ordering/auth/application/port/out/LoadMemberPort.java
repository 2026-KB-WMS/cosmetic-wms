package com.kb.ordering.auth.application.port.out;

import com.kb.ordering.auth.application.port.out.dto.MemberInfo;

public interface LoadMemberPort {

    MemberInfo loadById(Long memberId);
}
