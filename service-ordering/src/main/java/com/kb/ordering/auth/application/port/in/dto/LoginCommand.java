package com.kb.ordering.auth.application.port.in.dto;

public record LoginCommand(
        String loginId,
        String password
) {
}
