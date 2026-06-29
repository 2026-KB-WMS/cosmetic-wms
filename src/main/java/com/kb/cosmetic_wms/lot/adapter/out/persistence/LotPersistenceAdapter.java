package com.kb.cosmetic_wms.lot.adapter.out.persistence;

import com.kb.cosmetic_wms.lot.application.port.out.LotPort;
import com.kb.cosmetic_wms.lot.domain.model.Lot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LotPersistenceAdapter implements LotPort {

    private final LotJpaRepository lotJpaRepository;

    @Override
    public boolean existsByInboundIdAndManufacturerLotNumber(Long inboundId, String manufacturerLotNumber) {
        return lotJpaRepository.existsByInboundIdAndManufacturerLotNumber(inboundId, manufacturerLotNumber);
    }

    @Override
    public Optional<Lot> findByInboundIdAndManufacturerLotNumber(Long inboundId, String manufacturerLotNumber) {
        return lotJpaRepository.findByInboundIdAndManufacturerLotNumber(inboundId, manufacturerLotNumber)
                .map(LotEntity::toDomain);
    }

    @Override
    public Optional<Lot> findById(Long id) {
        return lotJpaRepository.findById(id).map(LotEntity::toDomain);
    }

    @Override
    public List<Lot> findByProductId(Long productId) {
        return lotJpaRepository.findByProductId(productId).stream()
                .map(LotEntity::toDomain)
                .toList();
    }

    @Override
    public Lot save(Lot lot) {
        return lotJpaRepository.save(LotEntity.fromDomain(lot)).toDomain();
    }

    @Override
    public void delete(Lot lot) {
        lotJpaRepository.deleteById(lot.getId());
    }
}
