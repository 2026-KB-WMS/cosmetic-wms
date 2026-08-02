package com.kb.common.security;

import java.util.Optional;

public interface JwtClaimExtractor {

    Optional<AuthenticatedMember> extract(String token);
}
