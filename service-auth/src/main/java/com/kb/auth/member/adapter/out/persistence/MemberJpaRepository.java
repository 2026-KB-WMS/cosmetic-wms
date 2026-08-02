package com.kb.auth.member.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
