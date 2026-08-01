package com.kb.auth.auth.adapter.out.token;

import com.kb.auth.auth.application.port.out.TokenProvider;
import com.kb.auth.member.domain.model.Role;
import org.springframework.stereotype.Component;

@Component
public class StubTokenProvider implements TokenProvider {

    @Override
    public String issue(Long memberId, Role role) {
        return "stub-token." + memberId + "." + role.name();
    }
}
