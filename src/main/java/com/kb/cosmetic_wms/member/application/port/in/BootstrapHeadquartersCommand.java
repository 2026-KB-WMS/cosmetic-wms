package com.kb.cosmetic_wms.member.application.port.in;

public record BootstrapHeadquartersCommand(
        String loginId,
        String password,
        String memberName,
        String email,
        String phoneNumber
) {
}
