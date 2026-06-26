package com.kb.cosmetic_wms.member.application.port.in;

public record LoginMemberCommand(
        String loginId,
        String password
) {
}