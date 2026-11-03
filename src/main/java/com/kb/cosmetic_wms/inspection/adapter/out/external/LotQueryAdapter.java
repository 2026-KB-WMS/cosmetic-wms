package com.kb.cosmetic_wms.inspection.adapter.out.external;

import com.kb.cosmetic_wms.inspection.application.port.out.LotQueryPort;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionLotNotFoundException;
import com.kb.cosmetic_wms.lot.application.port.in.FindLotUseCase;
import com.kb.cosmetic_wms.lot.application.port.in.LotResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LotQueryAdapter implements LotQueryPort {

    private final FindLotUseCase findLotUseCase;

    @Override
    public Long findLotIdByInboundAndManufacturerLot(Long inboundId, String manufacturerLotNumber) {
        return findLotUseCase.findByInboundIdAndManufacturerLotNumber(inboundId, manufacturerLotNumber)
                .map(LotResult::id)
                .orElseThrow(InspectionLotNotFoundException::new);
    }
}
