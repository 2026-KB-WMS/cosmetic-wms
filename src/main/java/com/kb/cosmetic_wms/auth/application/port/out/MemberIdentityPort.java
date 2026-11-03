package com.kb.cosmetic_wms.auth.application.port.out;

import java.util.Optional;

public interface MemberIdentityPort {

    MemberIdentity authenticate(String loginId, String password);

    Optional<MemberIdentity> findById(Long memberId);
}
