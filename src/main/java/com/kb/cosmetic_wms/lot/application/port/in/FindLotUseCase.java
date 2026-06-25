package com.kb.cosmetic_wms.lot.application.port.in;

import java.util.List;

public interface FindLotUseCase {
    LotResult findById(Long lotId);
    List<LotResult> findByProductId(Long productId);
}