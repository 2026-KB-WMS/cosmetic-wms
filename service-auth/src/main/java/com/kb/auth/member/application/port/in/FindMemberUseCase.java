package com.kb.auth.member.application.port.in;

import com.kb.auth.member.application.port.in.dto.MemberResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindMemberUseCase {

    MemberResult findById(Long id);

    Page<MemberResult> findAll(Pageable pageable);
}