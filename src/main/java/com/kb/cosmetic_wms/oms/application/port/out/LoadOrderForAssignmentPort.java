package com.kb.cosmetic_wms.oms.application.port.out;

import java.util.List;
import java.util.Optional;

public interface LoadOrderForAssignmentPort {

    Optional<OrderForAssignment> load(Long orderId);

    record OrderForAssignment(Long orderId, Long storeId, boolean awaitingAssignment, List<Item> items) {

        public record Item(Long orderItemId, Long productId, int quantity) {
        }
    }
}
