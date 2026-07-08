package com.kb.cosmetic_wms.inspection.application.service;

import com.kb.cosmetic_wms.inspection.application.port.in.RecordInspectionFailureCommand;
import com.kb.cosmetic_wms.inspection.application.port.in.RecordInspectionFailureUseCase;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionCreationFailurePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InspectionFailureService implements RecordInspectionFailureUseCase {

    private final InspectionCreationFailurePort inspectionCreationFailurePort;

    /**
     * 실패 이력은 원 트랜잭션 롤백과 무관하게 반드시 남아야 하므로 REQUIRES_NEW로 격리한다.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(RecordInspectionFailureCommand command) {
        inspectionCreationFailurePort.save(new InspectionCreationFailurePort.InspectionCreationFailure(
                command.inboundId(),
                command.lineId(),
                command.productId(),
                command.warehouseId(),
                command.receivedQuantity(),
                command.manufacturerLotNumber(),
                command.expirationDate(),
                command.errorMessage()
        ));
    }
}
