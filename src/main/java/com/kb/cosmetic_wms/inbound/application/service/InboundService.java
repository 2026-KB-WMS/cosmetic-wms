package com.kb.cosmetic_wms.inbound.application.service;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.global.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.inbound.application.exception.InboundPartnerNotFoundException;
import com.kb.cosmetic_wms.inbound.application.exception.InboundWarehouseNotFoundException;
import com.kb.cosmetic_wms.inbound.application.port.in.*;
import com.kb.cosmetic_wms.inbound.application.port.out.InboundPort;
import com.kb.cosmetic_wms.inbound.application.port.out.PartnerQueryPort;
import com.kb.cosmetic_wms.inbound.application.port.out.ProductQueryPort;
import com.kb.cosmetic_wms.inbound.application.port.out.StorageQueryPort;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundEmptyItemsException;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundNotFoundException;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundProductNotFoundException;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
import com.kb.cosmetic_wms.inbound.domain.model.InboundItem;
import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InboundService implements InboundLifecycleUseCase, InboundItemUseCase, FindInboundUseCase {

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
        Inbound inbound = Inbound.create(command.inboundDate(), command.warehouseId(), command.partnerId());
        return InboundResult.from(inboundPort.save(inbound));
    }

    @Override
    @Transactional
    public InboundResult addItem(Long inboundId, AddInboundItemCommand command) {
        Inbound inbound = findInboundOrThrow(inboundId);
        if (!productQueryPort.existsById(command.productId())) {
            throw new InboundProductNotFoundException();
        }
        InboundLine line = new InboundLine(command.productId(), command.quantity(),
                command.manufactureDate(), command.expirationDate());
        inbound.addItem(line);
        return InboundResult.from(inboundPort.save(inbound));
    }

    @Override
    @Transactional
    public InboundResult start(Long inboundId) {
        Inbound inbound = findInboundOrThrow(inboundId);
        if (inbound.getInboundItems().isEmpty()) {
            throw new InboundEmptyItemsException();
        }
        inbound.startExecution();
        return InboundResult.from(inboundPort.save(inbound));
    }

    @Override
    @Transactional
    public InboundItemResult completePutaway(Long inboundId, Long itemId, PutawayCommand command) {
        Inbound inbound = findInboundOrThrow(inboundId);
        InboundItem item = inbound.putawayItem(itemId, command.lotId(), command.sectionId());
        inboundPort.save(inbound);
        return InboundItemResult.from(item);
    }

    @Override
    @Transactional
    public InboundItemResult approve(Long inboundId, Long itemId) {
        Inbound inbound = findInboundOrThrow(inboundId);
        InboundItem item = inbound.approveItem(itemId);
        inboundPort.save(inbound);
        return InboundItemResult.from(item);
    }

    @Override
    @Transactional
    public InboundItemResult hold(Long inboundId, Long itemId) {
        Inbound inbound = findInboundOrThrow(inboundId);
        InboundItem item = inbound.holdItem(itemId);
        inboundPort.save(inbound);
        return InboundItemResult.from(item);
    }

    @Override
    @Transactional
    public InboundResult complete(Long inboundId) {
        Inbound inbound = inboundPort.findByIdWithItemsForUpdate(inboundId)
                .orElseThrow(InboundNotFoundException::new);
        inbound.completeExecution();
        Inbound saved = inboundPort.save(inbound);
        eventPublisher.publish(toInboundCompletedEvent(saved));
        return InboundResult.from(saved);
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
        return inboundPort.findByIdWithItems(inboundId).orElseThrow(InboundNotFoundException::new);
    }

    private InboundCompletedEvent toInboundCompletedEvent(Inbound inbound) {
        var snapshots = inbound.getInboundItems().stream()
                .map(item -> new InboundCompletedEvent.ItemSnapshot(
                        item.getId(), item.getProductId(), item.getLotId(),
                        item.getSectionId(), item.getQuantity(), item.getExpirationDate()
                ))
                .toList();
        return new InboundCompletedEvent(inbound.getId(), inbound.getWarehouseId(), snapshots);
    }
}
