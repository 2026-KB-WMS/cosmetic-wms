package com.kb.ordering.auth.application.port.out;

import com.kb.ordering.member.domain.model.Role;

public interface TokenProvider {

    String issue(Long memberId, Role role);
}
