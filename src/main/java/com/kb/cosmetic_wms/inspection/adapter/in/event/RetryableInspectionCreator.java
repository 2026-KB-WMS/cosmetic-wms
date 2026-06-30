package com.kb.cosmetic_wms.inspection.adapter.in.event;

import com.kb.cosmetic_wms.inbound.application.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionCommand;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionCreationFailurePort;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RetryableInspectionCreator {

    private final CreateInspectionUseCase createInspectionUseCase;
    private final InspectionCreationFailurePort inspectionCreationFailurePort;

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void createWithRetry(InboundCompletedEvent event, InboundCompletedEvent.LineSnapshot line) {
        createInspectionUseCase.create(new CreateInspectionCommand(
                InspectionSourceType.INBOUND,
                line.lineId(),
                line.receivedQuantity(),
                line.productId(),
                event.inboundId(),
                line.manufacturerLotNumber(),
                event.warehouseId(),
                line.expirationDate() != null ? line.expirationDate().toLocalDate() : null
        ));
    }

    @Recover
    public void recover(Exception ex, InboundCompletedEvent event, InboundCompletedEvent.LineSnapshot line) {
        inspectionCreationFailurePort.save(event, line, ex.getMessage());
    }
}