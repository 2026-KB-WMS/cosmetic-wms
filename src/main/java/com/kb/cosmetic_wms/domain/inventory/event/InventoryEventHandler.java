package com.kb.cosmetic_wms.domain.inventory.event;

import com.kb.cosmetic_wms.domain.inspection.event.InspectionCompletedEvent;
import com.kb.cosmetic_wms.domain.inventory.service.InventoryService;
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
    public void onInspectionCompleted(InspectionCompletedEvent event) {
        // 아이템 조회 및 재고 생성 로직은 출처 도메인(inbound/return) 핸들러에서 담당
    }
}
