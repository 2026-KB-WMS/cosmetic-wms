package com.kb.cosmetic_wms.oms.application.service;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.oms.application.port.in.AssignWarehouseCommand;
import com.kb.cosmetic_wms.oms.application.port.in.AssignWarehouseUseCase;
import com.kb.cosmetic_wms.oms.application.port.in.WarehouseAssignmentResult;
import com.kb.cosmetic_wms.oms.application.port.out.AssignOrderWarehousePort;
import com.kb.cosmetic_wms.oms.application.port.out.LoadStoreLocationPort;
import com.kb.cosmetic_wms.oms.application.port.out.LoadWarehouseCandidatesPort;
import com.kb.cosmetic_wms.oms.application.port.out.RoutingPort;
import com.kb.cosmetic_wms.oms.domain.event.WarehouseAssignedEvent;
import com.kb.cosmetic_wms.oms.domain.model.DemandLine;
import com.kb.cosmetic_wms.oms.domain.model.WarehouseCandidate;
import com.kb.cosmetic_wms.oms.domain.service.WeightBasedAssignmentPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
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
