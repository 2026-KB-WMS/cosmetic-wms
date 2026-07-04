package com.kb.cosmetic_wms.inspection.adapter.in.event;

import com.kb.cosmetic_wms.inbound.application.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionCommand;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.RecordInspectionFailureCommand;
import com.kb.cosmetic_wms.inspection.application.port.in.RecordInspectionFailureUseCase;
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
    private final RecordInspectionFailureUseCase recordInspectionFailureUseCase;

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
        recordInspectionFailureUseCase.record(new RecordInspectionFailureCommand(
                event.inboundId(),
                line.lineId(),
                line.productId(),
                event.warehouseId(),
                line.receivedQuantity(),
                line.manufacturerLotNumber(),
                line.expirationDate() != null ? line.expirationDate().toLocalDate() : null,
                ex.getMessage()
        ));
    }
}