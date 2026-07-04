package com.kb.cosmetic_wms.storage.adapter.in.event;

import com.kb.cosmetic_wms.inbound.domain.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.storage.application.port.in.UpdateDockingCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.UpdateDockingCapacityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component("storageInboundCompletedEventAdapter")
@RequiredArgsConstructor
public class InboundCompletedEventAdapter {

    private final UpdateDockingCapacityUseCase updateDockingCapacityUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(InboundCompletedEvent event) {
        List<UpdateDockingCapacityCommand.LineItem> lines = event.lines().stream()
                .map(l -> new UpdateDockingCapacityCommand.LineItem(l.productId(), l.receivedQuantity()))
                .toList();

        updateDockingCapacityUseCase.updateDockingCapacity(
                new UpdateDockingCapacityCommand(event.warehouseId(), lines)
        );
    }
}
