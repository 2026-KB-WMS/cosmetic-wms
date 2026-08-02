package com.kb.auth.member.adapter.in.web.dto;

import com.kb.auth.member.application.port.in.dto.ChangeRoleCommand;
import com.kb.auth.member.domain.model.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull(message = "변경할 역할은 필수 입력 항목입니다.")
        Role role
) {
    public ChangeRoleCommand toCommand(Long memberId) {
        return new ChangeRoleCommand(memberId, role);
    }
}
