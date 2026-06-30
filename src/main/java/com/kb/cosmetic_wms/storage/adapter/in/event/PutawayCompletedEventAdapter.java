package com.kb.cosmetic_wms.storage.adapter.in.event;

import com.kb.cosmetic_wms.global.event.PutawayCompletedEvent;
import com.kb.cosmetic_wms.storage.application.port.in.IncreaseSectionCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.IncreaseSectionCapacityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PutawayCompletedEventAdapter {

    private final IncreaseSectionCapacityUseCase increaseSectionCapacityUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PutawayCompletedEvent event) {
        increaseSectionCapacityUseCase.increaseSectionCapacity(
                new IncreaseSectionCapacityCommand(
                        event.warehouseId(),
                        event.targetSectionId(),
                        event.quantity()
                )
        );
    }
}
