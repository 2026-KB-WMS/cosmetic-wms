package com.kb.ordering.assignment.adapter.out.event;

import com.kb.contracts.ordering.Topics;
import com.kb.ordering.assignment.application.port.out.EventPublisher;
import com.kb.ordering.assignment.domain.event.WarehouseAssignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentKafkaEventPublisherAdapter implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishWarehouseAssigned(WarehouseAssignedEvent event) {
        kafkaTemplate.send(Topics.WAREHOUSE_ASSIGNED, String.valueOf(event.orderId()), toContract(event));
    }

    private com.kb.contracts.ordering.WarehouseAssignedEvent toContract(WarehouseAssignedEvent e) {
        return new com.kb.contracts.ordering.WarehouseAssignedEvent(
                e.orderId(),
                e.warehouseId(),
                e.items().stream()
                        .map(i -> new com.kb.contracts.ordering.WarehouseAssignedEvent.ItemSnapshot(
                                i.orderItemId(), i.productId(), i.quantity()))
                        .toList()
        );
    }
}