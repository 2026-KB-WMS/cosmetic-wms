package com.kb.cosmetic_wms.domain.member.repository;

import com.kb.cosmetic_wms.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByLoginId(String loginId);
}
