package com.kb.ordering.auth.application.port.out;

import com.kb.ordering.auth.application.port.out.dto.MemberInfo;
import com.kb.ordering.auth.application.port.out.dto.MemberRegistration;

public interface RegisterMemberPort {

    MemberInfo register(MemberRegistration registration);
}
