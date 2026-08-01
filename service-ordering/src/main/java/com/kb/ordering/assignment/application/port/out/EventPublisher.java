package com.kb.ordering.assignment.application.port.out;

import com.kb.ordering.assignment.domain.event.WarehouseAssignedEvent;

public interface EventPublisher {
    void publishWarehouseAssigned(WarehouseAssignedEvent event);
}
