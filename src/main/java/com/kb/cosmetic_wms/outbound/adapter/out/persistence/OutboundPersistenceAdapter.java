package com.kb.cosmetic_wms.outbound.adapter.out.persistence;

import com.kb.cosmetic_wms.outbound.application.port.out.OutboundPort;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OutboundPersistenceAdapter implements OutboundPort {

    private final OutboundJpaRepository outboundJpaRepository;

    @Override
    public Optional<Outbound> findByIdForUpdate(Long id) {
        return outboundJpaRepository.findByIdForUpdate(id).map(OutboundEntity::toDomain);
    }

    @Override
    public Optional<Outbound> findByIdWithItemsForUpdate(Long id) {
        return outboundJpaRepository.findByIdWithItemsForUpdate(id).map(OutboundEntity::toDomain);
    }

    @Override
    public Outbound save(Outbound outbound) {
        return outboundJpaRepository.save(OutboundEntity.fromDomain(outbound)).toDomain();
    }
}