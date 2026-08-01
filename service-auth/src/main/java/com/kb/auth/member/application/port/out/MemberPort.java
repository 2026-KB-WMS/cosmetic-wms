package com.kb.auth.member.application.port.out;

import com.kb.auth.member.domain.model.Member;

import java.util.Optional;

public interface MemberPort {

    boolean existsByEmail(String email);

    Optional<Member> findById(Long id);

    Member save(Member member);
}
