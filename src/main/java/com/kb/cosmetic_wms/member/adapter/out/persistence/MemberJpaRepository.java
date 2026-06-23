package com.kb.cosmetic_wms.member.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {

    boolean existsByLoginId(String loginId);

    Optional<MemberEntity> findByLoginId(String loginId);
}