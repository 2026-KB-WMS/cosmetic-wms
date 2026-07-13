package com.kb.ordering.auth.adapter.out.token;

import com.kb.ordering.auth.application.port.out.TokenProvider;
import com.kb.ordering.member.domain.model.Role;
import org.springframework.stereotype.Component;

@Component
public class StubTokenProvider implements TokenProvider {

    @Override
    public String issue(Long memberId, Role role) {
        return "stub-token." + memberId + "." + role.name();
    }
}
