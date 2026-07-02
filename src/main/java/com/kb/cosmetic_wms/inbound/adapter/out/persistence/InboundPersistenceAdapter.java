package com.kb.cosmetic_wms.inbound.adapter.out.persistence;

import com.kb.cosmetic_wms.inbound.application.port.out.InboundPort;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundNotFoundException;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InboundPersistenceAdapter implements InboundPort {

    private final InboundJpaRepository inboundJpaRepository;

    @Override
    public Optional<Inbound> findByIdWithLines(Long id) {
        return inboundJpaRepository.findByIdWithLines(id).map(InboundEntity::toDomain);
    }

    @Override
    public Optional<Inbound> findByIdWithLinesForUpdate(Long id) {
        // 1단계: inbound 행만 FOR UPDATE로 잠금 (inbound_line gap lock 방지)
        if (inboundJpaRepository.findByIdForUpdate(id).isEmpty()) {
            return Optional.empty();
        }
        // 2단계: lines를 별도 SELECT로 로드 (동일 트랜잭션이므로 캐시된 관리 엔티티에 컬렉션이 채워진다)
        return inboundJpaRepository.findByIdWithLines(id).map(InboundEntity::toDomain);
    }

    @Override
    public Inbound save(Inbound inbound) {
        if (inbound.getId() == null) {
            return inboundJpaRepository.save(InboundEntity.fromDomain(inbound)).toDomain();
        }
        // UPDATE 경로: 관리 엔티티를 직접 수정해 orphanRemoval DELETE+INSERT 패턴을 방지
        // fromDomain()으로 새 인스턴스를 만들면 JPA가 기존 lines를 DELETE 후 INSERT하여 Deadlock 발생
        InboundEntity managed = inboundJpaRepository.findByIdWithLines(inbound.getId())
                .orElseThrow(InboundNotFoundException::new);
        managed.updateFrom(inbound);
        return managed.toDomain();
    }
}