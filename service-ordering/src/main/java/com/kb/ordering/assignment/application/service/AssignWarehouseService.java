package com.kb.ordering.assignment.application.service;

import com.kb.common.event.EventPublisher;
import com.kb.ordering.assignment.application.port.in.AssignWarehouseCommand;
import com.kb.ordering.assignment.application.port.in.AssignWarehouseUseCase;
import com.kb.ordering.assignment.application.port.in.WarehouseAssignmentResult;
import com.kb.ordering.assignment.application.port.out.AssignOrderWarehousePort;
import com.kb.ordering.assignment.application.port.out.LoadStoreLocationPort;
import com.kb.ordering.assignment.application.port.out.LoadWarehouseCandidatesPort;
import com.kb.ordering.assignment.application.port.out.RoutingPort;
import com.kb.ordering.assignment.domain.event.WarehouseAssignedEvent;
import com.kb.ordering.assignment.domain.model.DemandLine;
import com.kb.ordering.assignment.domain.model.WarehouseCandidate;
import com.kb.ordering.assignment.domain.service.WeightBasedAssignmentPolicy;
import com.kb.ordering.global.geocoding.GeoCoordinate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// @Service
@RequiredArgsConstructor
public class AssignWarehouseService implements AssignWarehouseUseCase {

    private final LoadStoreLocationPort loadStoreLocationPort;
    private final LoadWarehouseCandidatesPort loadWarehouseCandidatesPort;
    private final RoutingPort routingPort;
    private final WeightBasedAssignmentPolicy assignmentPolicy;
    private final AssignOrderWarehousePort assignOrderWarehousePort;
    private final EventPublisher eventPublisher;

    /**
     * 창고 확정(order) → WarehouseAssignedEvent 발행 → 출고 전표 생성·재고 할당(outbound, BEFORE_COMMIT 구독)이
     * 모두 이 트랜잭션 안에서 처리된다. 어느 단계든 실패하면 배정 자체가 롤백되어 정합성 유지
     */
    @Override
    @Transactional
    public WarehouseAssignmentResult assign(AssignWarehouseCommand command) {
        GeoCoordinate destination = loadStoreLocationPort.loadStoreLocation(command.storeId());
        List<DemandLine> demands = command.items().stream()
                .map(item -> new DemandLine(item.productId(), item.quantity()))
                .toList();
        List<WarehouseCandidate> candidates = loadWarehouseCandidatesPort.loadCandidates(
                demands.stream().map(DemandLine::productId).toList());

        WarehouseCandidate selected = assignmentPolicy.assign(
                destination, candidates, demands, LocalDate.now(), routingPort::drivingDistanceMeters);

        assignOrderWarehousePort.assignWarehouse(command.orderId(), selected.getWarehouseId());
        eventPublisher.publish(toWarehouseAssignedEvent(command, selected.getWarehouseId()));

        return new WarehouseAssignmentResult(command.orderId(), selected.getWarehouseId());
    }

    private WarehouseAssignedEvent toWarehouseAssignedEvent(AssignWarehouseCommand command, Long warehouseId) {
        List<WarehouseAssignedEvent.ItemSnapshot> items = command.items().stream()
                .map(item -> new WarehouseAssignedEvent.ItemSnapshot(
                        item.orderItemId(), item.productId(), item.quantity()))
                .toList();
        return new WarehouseAssignedEvent(command.orderId(), warehouseId, items);
    }
}
