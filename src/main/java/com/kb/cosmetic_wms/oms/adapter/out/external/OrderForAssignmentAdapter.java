package com.kb.cosmetic_wms.oms.adapter.out.external;

import com.kb.cosmetic_wms.oms.application.port.out.LoadOrderForAssignmentPort;
import com.kb.cosmetic_wms.order.application.port.in.FindOrderAssignmentTargetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderForAssignmentAdapter implements LoadOrderForAssignmentPort {

    private final FindOrderAssignmentTargetUseCase findOrderAssignmentTargetUseCase;

    @Override
    public Optional<OrderForAssignment> load(Long orderId) {
        return findOrderAssignmentTargetUseCase.findAssignmentTarget(orderId)
                .map(target -> new OrderForAssignment(
                        target.orderId(),
                        target.storeId(),
                        target.awaitingAssignment(),
                        target.items().stream()
                                .map(item -> new OrderForAssignment.Item(
                                        item.orderItemId(), item.productId(), item.quantity()))
                                .toList()));
    }
}
