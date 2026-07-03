package com.kb.cosmetic_wms.oms.application.port.in;

public interface AssignWarehouseUseCase {

    WarehouseAssignmentResult assign(AssignWarehouseCommand command);
}
