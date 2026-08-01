package com.kb.ordering.assignment.application.port.out;

public interface AssignOrderWarehousePort {

    /**
     * 배정 결과를 발주(order 컨텍스트)에 반영해 창고를 확정한다.
     */
    void assignWarehouse(Long orderId, Long warehouseId);
}
