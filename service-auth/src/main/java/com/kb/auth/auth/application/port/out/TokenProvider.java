package com.kb.auth.auth.application.port.out;

import com.kb.auth.member.domain.model.Role;

public interface TokenProvider {

    String issue(Long memberId, Role role);
}
