package com.kb.auth.auth.application.port.out;

public interface TokenParser {

    Long extractMemberId(String token);
}
