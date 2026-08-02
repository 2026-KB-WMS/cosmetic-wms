package com.kb.auth.member.application.port.in.dto;

import com.kb.auth.member.domain.model.Role;

public record ChangeRoleCommand(Long memberId, Role role) {
}
