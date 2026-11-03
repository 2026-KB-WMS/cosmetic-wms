package com.kb.cosmetic_wms.member.application.port.in;

import java.util.Optional;

public interface MemberIdentityUseCase {

    MemberIdentityResult authenticate(LoginMemberCommand command);

    Optional<MemberIdentityResult> findIdentity(Long memberId);
}
