package com.kb.auth.member.adapter.out.persistence;

import com.kb.auth.member.application.port.out.MemberPort;
import com.kb.auth.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements MemberPort {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id)
                .map(MemberEntity::toDomain);
    }

    @Override
    public Member save(Member member) {
        MemberEntity entity = memberJpaRepository.save(MemberEntity.fromDomain(member));
        return entity.toDomain();
    }
}
