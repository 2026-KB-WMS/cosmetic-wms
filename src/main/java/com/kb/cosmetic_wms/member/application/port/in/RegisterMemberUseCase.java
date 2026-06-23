package com.kb.cosmetic_wms.member.application.port.in;

public interface RegisterMemberUseCase {
    
    MemberResult register(RegisterMemberCommand command);
}
