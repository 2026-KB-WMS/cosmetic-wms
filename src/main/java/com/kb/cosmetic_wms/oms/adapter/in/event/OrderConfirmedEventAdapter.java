package com.kb.cosmetic_wms.oms.adapter.in.event;

import com.kb.cosmetic_wms.order.domain.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 발주 확정 커밋 이후(AFTER_COMMIT) 비동기로 최적 창고 배정을 트리거한다.
 * 외부 Routing API 호출이 포함되므로 발주 확정 트랜잭션과 분리한다.
 */
@Component("omsOrderConfirmedEventAdapter")
@RequiredArgsConstructor
public class OrderConfirmedEventAdapter {

    private final RetryableWarehouseAssigner retryableWarehouseAssigner;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        retryableWarehouseAssigner.assignWithRetry(event);
    }
}
