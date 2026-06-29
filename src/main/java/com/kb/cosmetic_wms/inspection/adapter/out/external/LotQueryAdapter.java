package com.kb.cosmetic_wms.inspection.adapter.out.external;

import com.kb.cosmetic_wms.inspection.application.port.out.LotQueryPort;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionLotNotFoundException;
import com.kb.cosmetic_wms.lot.application.port.out.LotPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LotQueryAdapter implements LotQueryPort {

    private final LotPort lotPort;

    @Override
    public Long findLotIdByInboundAndManufacturerLot(Long inboundId, String manufacturerLotNumber) {
        return lotPort.findByInboundIdAndManufacturerLotNumber(inboundId, manufacturerLotNumber)
                .map(lot -> lot.getId())
                .orElseThrow(InspectionLotNotFoundException::new);
    }
}
