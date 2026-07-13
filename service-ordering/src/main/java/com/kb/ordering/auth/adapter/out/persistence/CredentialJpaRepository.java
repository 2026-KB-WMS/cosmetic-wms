package com.kb.ordering.auth.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CredentialJpaRepository extends JpaRepository<CredentialEntity, Long> {

    boolean existsByLoginId(String loginId);

    Optional<CredentialEntity> findByLoginId(String loginId);
}
