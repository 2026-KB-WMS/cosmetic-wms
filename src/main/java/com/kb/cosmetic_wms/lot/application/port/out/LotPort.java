package com.kb.cosmetic_wms.lot.application.port.out;

import com.kb.cosmetic_wms.lot.domain.model.Lot;

import java.util.List;
import java.util.Optional;

public interface LotPort {
    boolean existsByInboundIdAndManufacturerLotNumber(Long inboundId, String manufacturerLotNumber);
    Optional<Lot> findByInboundIdAndManufacturerLotNumber(Long inboundId, String manufacturerLotNumber);
    Optional<Lot> findById(Long id);
    List<Lot> findByProductId(Long productId);
    Lot save(Lot lot);
    void delete(Lot lot);
}
