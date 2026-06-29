package com.kb.cosmetic_wms.inspection.application.service;

import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionCommand;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionPort;
import com.kb.cosmetic_wms.inspection.application.port.out.LotQueryPort;
import com.kb.cosmetic_wms.inspection.domain.model.Inspection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateInspectionService implements CreateInspectionUseCase {

    private final InspectionPort inspectionPort;
    private final LotQueryPort lotQueryPort;

    @Override
    public void create(CreateInspectionCommand command) {
        Long lotId = lotQueryPort.findLotIdByInboundAndManufacturerLot(
                command.inboundId(), command.manufacturerLotNumber());
        Inspection inspection = Inspection.createPending(
                command.sourceType(), command.sourceId(),
                command.inspectionQuantity(), command.productId(), lotId,
                command.warehouseId(), command.expiryDate());
        inspectionPort.save(inspection);
    }
}
