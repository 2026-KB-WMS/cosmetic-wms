package com.kb.cosmetic_wms.member.adapter.out.persistence;

import com.kb.cosmetic_wms.member.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByRole(Role role);

    Optional<MemberEntity> findByLoginId(String loginId);
}