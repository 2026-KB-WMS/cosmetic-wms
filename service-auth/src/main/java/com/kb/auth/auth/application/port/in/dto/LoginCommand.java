package com.kb.auth.auth.application.port.in.dto;

public record LoginCommand(
        String loginId,
        String password
) {
}
