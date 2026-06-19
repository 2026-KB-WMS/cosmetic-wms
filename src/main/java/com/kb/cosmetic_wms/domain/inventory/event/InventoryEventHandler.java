package com.kb.cosmetic_wms.domain.inventory.event;

import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;
import com.kb.cosmetic_wms.domain.inbound.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.domain.inventory.dto.InboundPutawayCommand;
import com.kb.cosmetic_wms.domain.inventory.service.InventoryService;
import com.kb.cosmetic_wms.global.error.SecurityContextNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InventoryEventHandler {

    private final InventoryService inventoryService;
    private final AuditorAware<Long> auditorProvider;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onInboundCompleted(InboundCompletedEvent event) {
        Long memberId = auditorProvider.getCurrentAuditor()
                .orElseThrow(SecurityContextNotFoundException::new);
        event.items().stream()
                .filter(item -> item.inspectionStatus() == InspectionStatus.NORMAL)
                .map(item -> new InboundPutawayCommand(
                        item.productId(), item.lotId(), item.sectionId(),
                        event.warehouseId(), item.quantity(), event.inboundId(), memberId
                ))
                .forEach(inventoryService::createFromInbound);
    }
}
