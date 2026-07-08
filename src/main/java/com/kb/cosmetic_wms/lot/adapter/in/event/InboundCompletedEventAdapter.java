package com.kb.cosmetic_wms.lot.adapter.in.event;

import com.kb.cosmetic_wms.inbound.domain.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.lot.application.port.in.RegisterLotCommand;
import com.kb.cosmetic_wms.lot.application.port.in.RegisterLotUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component("lotInboundCompletedEventAdapter")
@RequiredArgsConstructor
public class InboundCompletedEventAdapter {

    private final RegisterLotUseCase registerLotUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(InboundCompletedEvent event) {
        for (InboundCompletedEvent.LineSnapshot line : event.lines()) {
            if (line.receivedQuantity() <= 0) {
                continue;
            }
            RegisterLotCommand command = new RegisterLotCommand(
                    event.inboundId(),
                    line.manufacturerLotNumber(),
                    line.manufacturingDate(),
                    line.expirationDate(),
                    line.productId()
            );
            registerLotUseCase.register(command);
        }
    }
}
