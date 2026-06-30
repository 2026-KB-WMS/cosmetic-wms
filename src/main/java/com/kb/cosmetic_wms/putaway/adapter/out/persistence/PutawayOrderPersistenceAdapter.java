package com.kb.cosmetic_wms.putaway.adapter.out.persistence;

import com.kb.cosmetic_wms.putaway.application.port.out.PutawayOrderPort;
import com.kb.cosmetic_wms.putaway.domain.model.PutawayOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PutawayOrderPersistenceAdapter implements PutawayOrderPort {

    private final PutawayOrderJpaRepository putawayOrderJpaRepository;

    @Override
    public PutawayOrder save(PutawayOrder putawayOrder) {
        PutawayOrderEntity entity = putawayOrderJpaRepository.save(PutawayOrderEntity.fromDomain(putawayOrder));
        return entity.toDomain();
    }

    @Override
    public Optional<PutawayOrder> findById(Long putawayOrderId) {
        return putawayOrderJpaRepository.findById(putawayOrderId)
                .map(PutawayOrderEntity::toDomain);
    }

    @Override
    public Optional<PutawayOrder> findByIdForUpdate(Long putawayOrderId) {
        return putawayOrderJpaRepository.findByIdForUpdate(putawayOrderId)
                .map(PutawayOrderEntity::toDomain);
    }
}
