package com.kb.cosmetic_wms.auth.application.port.out;

public interface AccessTokenPort {

    IssuedAccessToken issue(MemberIdentity identity);
}
