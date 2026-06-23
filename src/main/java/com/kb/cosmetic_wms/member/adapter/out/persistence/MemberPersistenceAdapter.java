package com.kb.cosmetic_wms.member.adapter.out.persistence;

import com.kb.cosmetic_wms.member.application.port.out.MemberPort;
import com.kb.cosmetic_wms.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements MemberPort {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public boolean existsByLoginId(String loginId) {
        return memberJpaRepository.existsByLoginId(loginId);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id)
                .map(MemberEntity::toDomain);
    }

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        return memberJpaRepository.findByLoginId(loginId)
                .map(MemberEntity::toDomain);
    }

    @Override
    public Member save(Member member) {
        MemberEntity entity = memberJpaRepository.save(MemberEntity.fromDomain(member));
        return entity.toDomain();
    }
}