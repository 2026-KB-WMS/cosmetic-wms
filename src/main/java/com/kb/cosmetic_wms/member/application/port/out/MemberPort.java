package com.kb.cosmetic_wms.member.application.port.out;

import com.kb.cosmetic_wms.member.domain.model.Member;

import java.util.Optional;

public interface MemberPort {

    boolean existsByLoginId(String loginId);

    Optional<Member> findById(Long id);

    Optional<Member> findByLoginId(String loginId);

    Member save(Member member);
}