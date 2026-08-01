package com.kb.auth.auth.fixture;

import com.kb.auth.auth.domain.model.Credential;

public class CredentialTestBuilder {

    private Long memberId = 1L;
    private String loginId = "user12345";
    private String encodedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    public CredentialTestBuilder memberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }

    public CredentialTestBuilder loginId(String loginId) {
        this.loginId = loginId;
        return this;
    }

    public CredentialTestBuilder encodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
        return this;
    }

    public Credential build() {
        return Credential.create(this.memberId, this.loginId, this.encodedPassword);
    }
}
