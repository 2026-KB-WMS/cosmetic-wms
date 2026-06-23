package com.kb.cosmetic_wms.member.application.port.in;

public interface LoginMemberUseCase {

    MemberResult login(LoginMemberCommand command);
}