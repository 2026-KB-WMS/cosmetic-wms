package com.kb.ordering.assignment.application.port.in;

public interface AssignWarehouseUseCase {

    WarehouseAssignmentResult assign(AssignWarehouseCommand command);
}
