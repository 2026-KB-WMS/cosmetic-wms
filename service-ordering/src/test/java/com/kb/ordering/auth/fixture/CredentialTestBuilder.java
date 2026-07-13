package com.kb.ordering.auth.fixture;

import com.kb.ordering.auth.domain.model.Credential;

public class CredentialTestBuilder {
    private Long memberId = 1L;
    private String loginId = "admin01";
    private String encodedPassword = "{bcrypt}encoded-password";

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
        return Credential.create(memberId, loginId, encodedPassword);
    }

    public Credential buildWithId(Long id) {
        return Credential.reconstitute(id, memberId, loginId, encodedPassword);
    }
}
