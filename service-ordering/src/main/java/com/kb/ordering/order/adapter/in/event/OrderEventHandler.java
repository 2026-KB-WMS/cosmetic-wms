package com.kb.ordering.order.adapter.in.event;

// TODO: Kafka 도입 후 아웃바운드 이벤트 수신 복원
// OutboundAllocatedEvent → startPreparation, OutboundShippedEvent → ship 연동 예정

// import com.kb.ordering.order.application.port.in.OrderLifecycleUseCase;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.event.TransactionPhase;
// import org.springframework.transaction.event.TransactionalEventListener;
//
// @Component
// @RequiredArgsConstructor
// public class OrderEventHandler {
//
//     private final OrderLifecycleUseCase orderLifecycleUseCase;
//
//     @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
//     public void onOutboundAllocated(OutboundAllocatedEvent event) {
//         orderLifecycleUseCase.startPreparation(event.ordersId());
//     }
//
//     @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
//     public void onOutboundShipped(OutboundShippedEvent event) {
//         orderLifecycleUseCase.ship(event.ordersId());
//     }
// }
