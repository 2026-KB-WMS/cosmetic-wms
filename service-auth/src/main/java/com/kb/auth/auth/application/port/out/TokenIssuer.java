package com.kb.auth.auth.application.port.out;

import com.kb.auth.member.domain.model.Role;

public interface TokenIssuer {

    String issueAccessToken(Long memberId, Role role);

    String issueRefreshToken(Long memberId);
}
