package com.kb.auth.auth.application.port.out;

import com.kb.auth.auth.application.port.out.dto.MemberInfo;

public interface LoadMemberPort {

    MemberInfo loadById(Long memberId);
}
