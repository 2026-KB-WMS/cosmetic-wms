package com.kb.cosmetic_wms.oms.application.port.in;

public record RecordAssignmentFailureCommand(Long orderId, Long storeId, String errorMessage) {
}
