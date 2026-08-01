package com.kb.auth.auth.domain.model;

import com.kb.auth.auth.domain.exception.CredentialValidationException;
import com.kb.auth.auth.domain.valueobject.LoginId;
import lombok.Getter;

@Getter
public class Credential {

    private final Long credentialId;
    private final Long memberId;
    private final LoginId loginId;
    private final String encodedPassword;

    private Credential(Long credentialId, Long memberId, LoginId loginId, String encodedPassword) {
        this.credentialId = credentialId;
        this.memberId = memberId;
        this.loginId = loginId;
        this.encodedPassword = encodedPassword;
    }

    public static Credential create(Long memberId, String loginId, String encodedPassword) {
        validateMemberId(memberId);
        validateEncodedPassword(encodedPassword);
        return new Credential(null, memberId, new LoginId(loginId), encodedPassword);
    }

    public static Credential reconstitute(Long credentialId, Long memberId, String loginId, String encodedPassword) {
        return new Credential(credentialId, memberId, new LoginId(loginId), encodedPassword);
    }

    private static void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new CredentialValidationException();
        }
    }

    private static void validateEncodedPassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new CredentialValidationException();
        }
    }
}
