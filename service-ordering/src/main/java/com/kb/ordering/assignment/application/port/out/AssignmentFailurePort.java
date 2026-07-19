package com.kb.ordering.assignment.application.port.out;

public interface AssignmentFailurePort {

    void save(Long orderId, Long storeId, String errorMessage);
}
