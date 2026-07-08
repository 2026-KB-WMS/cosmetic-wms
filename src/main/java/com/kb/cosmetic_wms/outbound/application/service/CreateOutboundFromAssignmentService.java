package com.kb.cosmetic_wms.outbound.application.service;

import com.kb.cosmetic_wms.outbound.application.port.in.CreateOutboundFromAssignmentCommand;
import com.kb.cosmetic_wms.outbound.application.port.in.CreateOutboundFromAssignmentUseCase;
import com.kb.cosmetic_wms.outbound.application.port.in.OutboundResult;
import com.kb.cosmetic_wms.outbound.application.port.out.FefoInventoryQueryPort;
import com.kb.cosmetic_wms.outbound.application.port.out.InventoryAllocationPort;
import com.kb.cosmetic_wms.outbound.application.port.out.OrderPreparationPort;
import com.kb.cosmetic_wms.outbound.application.port.out.OutboundPort;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.model.AvailableStock;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundLine;
import com.kb.cosmetic_wms.outbound.domain.service.FefoAllocationSelector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOutboundFromAssignmentService implements CreateOutboundFromAssignmentUseCase {

    private final OutboundPort outboundPort;
    private final FefoInventoryQueryPort fefoInventoryQueryPort;
    private final InventoryAllocationPort inventoryAllocationPort;
    private final OrderPreparationPort orderPreparationPort;
    private final FefoAllocationSelector fefoAllocationSelector;

    /**
     * 창고 배정 트랜잭션(BEFORE_COMMIT)에 합류해 실행되므로, 여기서 OutboundAllocatedEvent를
     * 발행하면 커밋 시점 스냅숏에 포함되지 않아 BEFORE_COMMIT 리스너가 동작하지 않는다.
     * 재고 할당·발주 전환은 이벤트 대신 out port 직접 호출로 처리한다.
     */
    @Override
    @Transactional
    public OutboundResult createFromAssignment(CreateOutboundFromAssignmentCommand command) {
        List<OutboundLine> lines = selectLinesWithFefo(command);

        Outbound outbound = Outbound.create(
                command.orderId(), command.warehouseId(), OutboundType.ORDER, lines);
        Outbound saved = outboundPort.save(outbound);

        for (OutboundLine line : lines) {
            inventoryAllocationPort.allocate(
                    line.inventoryId(), line.targetQuantity(), saved.getId(), command.memberId());
        }

        saved.allocate();
        saved = outboundPort.save(saved);
        orderPreparationPort.startPreparation(command.orderId());
        return OutboundResult.from(saved);
    }

    private List<OutboundLine> selectLinesWithFefo(CreateOutboundFromAssignmentCommand command) {
        List<OutboundLine> lines = new ArrayList<>();
        for (CreateOutboundFromAssignmentCommand.ItemDemand item : command.items()) {
            List<AvailableStock> stocks = fefoInventoryQueryPort.findAvailableForFefo(
                    item.productId(), command.warehouseId());
            lines.addAll(fefoAllocationSelector.select(item.orderItemId(), item.quantity(), stocks));
        }
        return lines;
    }
}
