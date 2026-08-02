package com.kb.auth.member.adapter.out.persistence;

import com.kb.auth.member.application.port.out.MemberPort;
import com.kb.auth.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public boolean existsByPhoneNumber(String phoneNumber) {
        return memberJpaRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id)
                .map(MemberEntity::toDomain);
    }

    @Override
    public Page<Member> findAll(Pageable pageable) {
        return memberJpaRepository.findAll(pageable)
                .map(MemberEntity::toDomain);
    }

    @Override
    public Member save(Member member) {
        MemberEntity entity = memberJpaRepository.save(MemberEntity.fromDomain(member));
        return entity.toDomain();
    }
}
