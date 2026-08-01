package com.kb.auth.auth.application.port.out;

import com.kb.auth.auth.domain.model.Credential;

import java.util.Optional;

public interface CredentialPort {

    boolean existsByLoginId(String loginId);

    Optional<Credential> findByLoginId(String loginId);

    Credential save(Credential credential);
}
