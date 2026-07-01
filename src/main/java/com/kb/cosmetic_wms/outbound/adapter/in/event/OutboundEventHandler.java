package com.kb.cosmetic_wms.outbound.adapter.in.event;

import com.kb.cosmetic_wms.inventory.application.port.in.FefoInventorySlice;
import com.kb.cosmetic_wms.inventory.application.port.in.FindFefoInventoryUseCase;
import com.kb.cosmetic_wms.inventory.application.port.in.InventoryStatusChangeCommand;
import com.kb.cosmetic_wms.inventory.application.port.in.ManageInventoryStatusUseCase;
import com.kb.cosmetic_wms.order.application.port.in.OrderLifecycleUseCase;
import com.kb.cosmetic_wms.outbound.application.port.out.OutboundPort;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundInsufficientStockException;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundLine;
import com.kb.cosmetic_wms.order.domain.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboundEventHandler {

    private final FindFefoInventoryUseCase findFefoInventoryUseCase;
    private final ManageInventoryStatusUseCase manageInventoryStatusUseCase;
    private final OutboundPort outboundPort;
    private final OrderLifecycleUseCase orderLifecycleUseCase;
    private final AuditorAware<Long> auditorProvider;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();

        List<OutboundLine> lines = selectWithFefo(event, actorId);

        Outbound outbound = Outbound.create(event.orderId(), event.warehouseId(), OutboundType.ORDER, lines);
        Outbound saved = outboundPort.save(outbound);

        for (OutboundLine line : lines) {
            manageInventoryStatusUseCase.allocate(line.inventoryId(),
                    new InventoryStatusChangeCommand(line.targetQuantity(), saved.getId(), actorId));
        }

        saved.allocate();
        outboundPort.save(saved);
        orderLifecycleUseCase.startPreparation(event.orderId());
    }

    private List<OutboundLine> selectWithFefo(OrderConfirmedEvent event, Long actorId) {
        List<OutboundLine> lines = new ArrayList<>();

        for (OrderConfirmedEvent.ItemSnapshot item : event.items()) {
            List<FefoInventorySlice> slots =
                    findFefoInventoryUseCase.findAvailableForFefo(item.productId(), event.warehouseId());

            int remaining = item.quantity();
            for (FefoInventorySlice slot : slots) {
                if (remaining <= 0) break;
                int take = Math.min(remaining, slot.availableQuantity());
                lines.add(new OutboundLine(item.orderItemId(), slot.inventoryId(), take));
                remaining -= take;
            }

            if (remaining > 0) {
                throw new OutboundInsufficientStockException();
            }
        }

        return lines;
    }
}
