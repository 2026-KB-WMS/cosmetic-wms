package com.kb.cosmetic_wms.outbound.adapter.out.persistence;

import com.kb.cosmetic_wms.outbound.application.port.out.OutboundPort;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
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
    public List<Outbound> findByIdsAndStatuses(Collection<Long> ids, Collection<OutboundStatus> statuses) {
        return outboundJpaRepository.findByIdInAndOutboundStatusInOrderByIdAsc(ids, statuses).stream()
                .map(OutboundEntity::toDomain)
                .toList();
    }

    @Override
    public Outbound save(Outbound outbound) {
        return outboundJpaRepository.save(OutboundEntity.fromDomain(outbound)).toDomain();
    }
}