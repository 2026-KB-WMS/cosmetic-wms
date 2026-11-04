package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import com.kb.cosmetic_wms.inventory.application.port.out.LotBlockPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LotBlockPersistenceAdapter implements LotBlockPort {

    private final InventoryLotBlockJpaRepository repository;

    @Override
    public void block(Long lotId, String status) {
        InventoryLotBlockEntity entity = repository.findById(lotId)
                .orElseGet(() -> InventoryLotBlockEntity.of(lotId, status));
        entity.changeStatus(status);
        repository.save(entity);
    }

    @Override
    public void release(Long lotId) {
        repository.deleteById(lotId);
    }

    @Override
    public boolean isBlocked(Long lotId) {
        return repository.existsById(lotId);
    }
}
