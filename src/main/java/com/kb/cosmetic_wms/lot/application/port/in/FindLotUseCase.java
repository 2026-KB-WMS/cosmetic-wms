package com.kb.cosmetic_wms.lot.application.port.in;

import java.util.List;
import java.util.Optional;

public interface FindLotUseCase {
    LotResult findById(Long lotId);
    List<LotResult> findByProductId(Long productId);
    Optional<LotResult> findByInboundIdAndManufacturerLotNumber(Long inboundId, String manufacturerLotNumber);
}