package com.kb.ordering.assignment.adapter.in.event;

import com.kb.contracts.ordering.OrderConfirmedEvent;
import com.kb.contracts.ordering.Topics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// @Component — AssignWarehouseService 활성화 후 함께 활성화
@RequiredArgsConstructor
public class OrderConfirmedKafkaConsumer {

    private final RetryableWarehouseAssigner retryableWarehouseAssigner;

    @KafkaListener(topics = Topics.ORDER_CONFIRMED)
    public void consume(OrderConfirmedEvent event) {
        retryableWarehouseAssigner.assignWithRetry(event);
    }
}