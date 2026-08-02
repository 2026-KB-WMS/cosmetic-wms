package com.kb.auth.auth.application.port.out;

import java.util.Optional;

public interface TokenParser {

    Optional<Long> extractMemberId(String token);
}
