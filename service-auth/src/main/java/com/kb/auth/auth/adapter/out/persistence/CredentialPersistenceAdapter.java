package com.kb.auth.auth.adapter.out.persistence;

import com.kb.auth.auth.application.port.out.CredentialPort;
import com.kb.auth.auth.domain.model.Credential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CredentialPersistenceAdapter implements CredentialPort {

    private final CredentialJpaRepository credentialJpaRepository;

    @Override
    public boolean existsByLoginId(String loginId) {
        return credentialJpaRepository.existsByLoginId(loginId);
    }

    @Override
    public Optional<Credential> findByLoginId(String loginId) {
        return credentialJpaRepository.findByLoginId(loginId)
                .map(CredentialEntity::toDomain);
    }

    @Override
    public Credential save(Credential credential) {
        CredentialEntity entity = credentialJpaRepository.save(CredentialEntity.fromDomain(credential));
        return entity.toDomain();
    }
}
