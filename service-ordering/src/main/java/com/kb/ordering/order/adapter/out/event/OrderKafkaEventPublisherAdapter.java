package com.kb.ordering.order.adapter.out.event;

import com.kb.contracts.ordering.Topics;
import com.kb.ordering.order.application.port.out.EventPublisher;
import com.kb.ordering.order.domain.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderKafkaEventPublisherAdapter implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        kafkaTemplate.send(Topics.ORDER_CONFIRMED, String.valueOf(event.orderId()), toContract(event));
    }

    private com.kb.contracts.ordering.OrderConfirmedEvent toContract(OrderConfirmedEvent e) {
        return new com.kb.contracts.ordering.OrderConfirmedEvent(
                e.orderId(),
                e.storeId(),
                e.items().stream()
                        .map(i -> new com.kb.contracts.ordering.OrderConfirmedEvent.ItemSnapshot(
                                i.orderItemId(), i.productId(), i.quantity()))
                        .toList()
        );
    }
}