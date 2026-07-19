package com.kb.ordering.assignment.adapter.in.event;

import com.kb.ordering.assignment.application.port.in.AssignWarehouseCommand;
import com.kb.ordering.assignment.application.port.in.AssignWarehouseUseCase;
import com.kb.ordering.assignment.application.port.out.AssignmentFailurePort;
import com.kb.ordering.assignment.domain.exception.AssignmentValidationException;
import com.kb.ordering.assignment.domain.exception.NoAssignableWarehouseException;
import com.kb.ordering.order.domain.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

// @Component
@RequiredArgsConstructor
public class RetryableWarehouseAssigner {

    private final AssignWarehouseUseCase assignWarehouseUseCase;
    private final AssignmentFailurePort assignmentFailurePort;

    @Retryable(
            retryFor = Exception.class,
            noRetryFor = {NoAssignableWarehouseException.class, AssignmentValidationException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void assignWithRetry(OrderConfirmedEvent event) {
        assignWarehouseUseCase.assign(new AssignWarehouseCommand(
                event.orderId(),
                event.storeId(),
                event.items().stream()
                        .map(item -> new AssignWarehouseCommand.ItemDemand(
                                item.orderItemId(), item.productId(), item.quantity()))
                        .toList()
        ));
    }

    @Recover
    public void recover(Exception ex, OrderConfirmedEvent event) {
        assignmentFailurePort.save(event.orderId(), event.storeId(), ex.getMessage());
    }
}
