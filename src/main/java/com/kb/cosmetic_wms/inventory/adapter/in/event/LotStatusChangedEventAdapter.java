package com.kb.cosmetic_wms.inventory.adapter.in.event;

import com.kb.cosmetic_wms.inventory.application.port.in.ManageLotBlockUseCase;
import com.kb.cosmetic_wms.lot.domain.event.LotStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 로트 상태 변경과 같은 트랜잭션(BEFORE_COMMIT)에서 차단 목록을 갱신한다.
 * 갱신에 실패하면 로트 상태 변경도 롤백되어, 로트는 리콜인데 재고는 풀려 있는 상태가 생기지 않는다.
 */
@Component
@RequiredArgsConstructor
public class LotStatusChangedEventAdapter {

    private final ManageLotBlockUseCase manageLotBlockUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onLotStatusChanged(LotStatusChangedEvent event) {
        if (event.allocatable()) {
            manageLotBlockUseCase.release(event.lotId());
            return;
        }
        manageLotBlockUseCase.block(event.lotId(), event.status());
    }
}
