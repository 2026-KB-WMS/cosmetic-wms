package com.kb.ordering.assignment.adapter.out.external;

import com.kb.ordering.assignment.application.port.out.AssignOrderWarehousePort;
import com.kb.ordering.order.application.port.in.OrderLifecycleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderWarehouseAssignmentAdapter implements AssignOrderWarehousePort {

    private final OrderLifecycleUseCase orderLifecycleUseCase;

    @Override
    public void assignWarehouse(Long orderId, Long warehouseId) {
        orderLifecycleUseCase.assignWarehouse(orderId, warehouseId);
    }
}
