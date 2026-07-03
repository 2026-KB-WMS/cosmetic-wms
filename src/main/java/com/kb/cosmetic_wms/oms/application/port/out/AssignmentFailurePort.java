package com.kb.cosmetic_wms.oms.application.port.out;

public interface AssignmentFailurePort {

    void save(Long orderId, Long storeId, String errorMessage);
}
