package com.kb.auth.member.application.port.out;

import com.kb.auth.member.domain.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberPort {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Member> findById(Long id);

    Page<Member> findAll(Pageable pageable);

    Member save(Member member);
}
