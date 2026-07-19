package com.kb.cosmetic_wms.outbound.adapter.in.event;

// TODO: Kafka 도입 후 WarehouseAssignedEvent 수신 복원
// WarehouseAssignedEvent → CreateOutboundFromAssignmentUseCase 연동 예정
// (ordering service가 Kafka로 발행하면 WMS가 구독)

// import com.kb.cosmetic_wms.oms.domain.event.WarehouseAssignedEvent;
// import com.kb.cosmetic_wms.outbound.application.port.in.CreateOutboundFromAssignmentCommand;
// import com.kb.cosmetic_wms.outbound.application.port.in.CreateOutboundFromAssignmentUseCase;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.AuditorAware;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.event.TransactionPhase;
// import org.springframework.transaction.event.TransactionalEventListener;
//
// @Component
// @RequiredArgsConstructor
// public class OutboundEventHandler {
//
//     private final CreateOutboundFromAssignmentUseCase createOutboundFromAssignmentUseCase;
//     private final AuditorAware<Long> auditorProvider;
//
//     @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
//     public void onWarehouseAssigned(WarehouseAssignedEvent event) {
//         Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();
//         createOutboundFromAssignmentUseCase.createFromAssignment(new CreateOutboundFromAssignmentCommand(
//                 event.orderId(),
//                 event.warehouseId(),
//                 actorId,
//                 event.items().stream()
//                         .map(item -> new CreateOutboundFromAssignmentCommand.ItemDemand(
//                                 item.orderItemId(), item.productId(), item.quantity()))
//                         .toList()
//         ));
//     }
// }
