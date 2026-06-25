package com.kb.cosmetic_wms.inbound.adapter.out.persistence;

import com.kb.cosmetic_wms.inbound.application.port.out.InboundPort;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InboundPersistenceAdapter implements InboundPort {

    private final InboundJpaRepository inboundJpaRepository;

    @Override
    public Optional<Inbound> findByIdWithItems(Long id) {
        return inboundJpaRepository.findByIdWithItems(id).map(InboundEntity::toDomain);
    }

    @Override
    public Optional<Inbound> findByIdWithItemsForUpdate(Long id) {
        return inboundJpaRepository.findByIdWithItemsForUpdate(id).map(InboundEntity::toDomain);
    }

    @Override
    public Inbound save(Inbound inbound) {
        return inboundJpaRepository.save(InboundEntity.fromDomain(inbound)).toDomain();
    }
}
