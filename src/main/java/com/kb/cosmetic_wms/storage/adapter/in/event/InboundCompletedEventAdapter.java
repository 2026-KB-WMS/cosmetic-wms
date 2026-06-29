package com.kb.cosmetic_wms.storage.adapter.in.event;

import com.kb.cosmetic_wms.inbound.application.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.storage.application.port.in.UpdateDockingCapacityUseCase;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("storageInboundCompletedEventAdapter")
@RequiredArgsConstructor
public class InboundCompletedEventAdapter {

    private final UpdateDockingCapacityUseCase updateDockingCapacityUseCase;
    private final ProductPort productPort;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(InboundCompletedEvent event) {
        List<Long> productIds = event.lines().stream()
                .filter(l -> l.receivedQuantity() > 0)
                .map(InboundCompletedEvent.LineSnapshot::productId)
                .distinct()
                .toList();

        if (productIds.isEmpty()) {
            return;
        }

        Map<Long, TemperatureType> tempByProductId = productPort.findTemperatureTypesByIds(productIds);

        Map<TemperatureZone, Integer> receivedByZone = event.lines().stream()
                .filter(l -> l.receivedQuantity() > 0)
                .collect(Collectors.groupingBy(
                        line -> toTemperatureZone(tempByProductId.get(line.productId())),
                        Collectors.summingInt(InboundCompletedEvent.LineSnapshot::receivedQuantity)
                ));

        updateDockingCapacityUseCase.updateDockingCapacity(event.warehouseId(), receivedByZone);
    }

    private TemperatureZone toTemperatureZone(TemperatureType temperatureType) {
        return switch (temperatureType) {
            case ROOM -> TemperatureZone.ROOM;
            case COOL -> TemperatureZone.COOL;
        };
    }
}
