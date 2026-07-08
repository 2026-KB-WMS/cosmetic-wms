package com.kb.cosmetic_wms.inbound.application.service;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.inbound.domain.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.inbound.application.exception.InboundCapacityExceededException;
import com.kb.cosmetic_wms.inbound.application.exception.InboundPartnerNotFoundException;
import com.kb.cosmetic_wms.inbound.application.exception.InboundWarehouseNotFoundException;
import com.kb.cosmetic_wms.inbound.application.port.in.*;
import com.kb.cosmetic_wms.inbound.application.port.out.InboundPort;
import com.kb.cosmetic_wms.inbound.application.port.out.PartnerQueryPort;
import com.kb.cosmetic_wms.inbound.application.port.out.ProductQueryPort;
import com.kb.cosmetic_wms.inbound.application.port.out.StorageQueryPort;
import com.kb.cosmetic_wms.inbound.application.port.out.StorageQueryPort.IncomingProduct;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundNotFoundException;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundProductNotFoundException;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;
import com.kb.cosmetic_wms.inbound.domain.model.ReceiveLineData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InboundService implements InboundLifecycleUseCase, FindInboundUseCase {

    private final InboundPort inboundPort;
    private final StorageQueryPort storageQueryPort;
    private final PartnerQueryPort partnerQueryPort;
    private final ProductQueryPort productQueryPort;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public InboundResult register(RegisterInboundCommand command) {
        if (!storageQueryPort.existsById(command.warehouseId())) {
            throw new InboundWarehouseNotFoundException();
        }
        if (!partnerQueryPort.existsById(command.partnerId())) {
            throw new InboundPartnerNotFoundException();
        }
        List<Long> productIds = command.lines().stream()
                .map(RegisterInboundCommand.LineItem::productId)
                .toList();
        if (!productQueryPort.allExistByIds(productIds)) {
            throw new InboundProductNotFoundException();
        }

        List<InboundLine> lines = command.lines().stream()
                .map(l -> InboundLine.create(l.productId(), l.orderedQuantity()))
                .toList();

        Inbound inbound = Inbound.create(command.inboundDate(), command.warehouseId(),
                command.partnerId(), lines);
        return InboundResult.from(inboundPort.save(inbound));
    }

    @Override
    @Transactional
    public InboundResult receive(Long inboundId, ReceiveInboundCommand command) {
        Inbound inbound = inboundPort.findByIdWithLinesForUpdate(inboundId)
                .orElseThrow(InboundNotFoundException::new);

        validateWarehouseCapacity(inbound, command);

        Map<Long, ReceiveLineData> receiveData = command.lines().stream()
                .collect(Collectors.toMap(
                        ReceiveInboundCommand.LineItem::lineId,
                        l -> new ReceiveLineData(l.receivedQuantity(), l.manufacturerLotNumber(),
                                l.manufacturingDate(), l.expirationDate())
                ));

        inbound.receive(receiveData);
        Inbound saved = inboundPort.save(inbound);
        eventPublisher.publish(toInboundCompletedEvent(saved));
        return InboundResult.from(saved);
    }

    private void validateWarehouseCapacity(Inbound inbound, ReceiveInboundCommand command) {
        Map<Long, Long> productIdByLineId = inbound.getInboundLines().stream()
                .collect(Collectors.toMap(InboundLine::getId, InboundLine::getProductId));

        List<IncomingProduct> items = command.lines().stream()
                .map(l -> new IncomingProduct(productIdByLineId.get(l.lineId()), l.receivedQuantity()))
                .toList();


        if (!storageQueryPort.canAccommodate(inbound.getWarehouseId(), items)) {
            throw new InboundCapacityExceededException();
        }
    }

    @Override
    @Transactional
    public InboundResult cancel(Long inboundId) {
        Inbound inbound = findInboundOrThrow(inboundId);
        inbound.cancel();
        return InboundResult.from(inboundPort.save(inbound));
    }

    @Override
    public InboundResult findById(Long inboundId) {
        return InboundResult.from(findInboundOrThrow(inboundId));
    }

    private Inbound findInboundOrThrow(Long inboundId) {
        return inboundPort.findByIdWithLines(inboundId).orElseThrow(InboundNotFoundException::new);
    }

    private InboundCompletedEvent toInboundCompletedEvent(Inbound inbound) {
        List<InboundCompletedEvent.LineSnapshot> snapshots = inbound.getInboundLines().stream()
                .map(line -> new InboundCompletedEvent.LineSnapshot(
                        line.getId(), line.getProductId(),
                        line.getOrderedQuantity(), line.getReceivedQuantity(),
                        line.getManufacturerLotNumber(),
                        line.getManufacturingDate(), line.getExpirationDate()
                ))
                .toList();
        return new InboundCompletedEvent(
                inbound.getId(),
                inbound.getInboundDate().toLocalDate(),
                inbound.getWarehouseId(),
                inbound.getPartnerId(),
                snapshots
        );
    }
}
