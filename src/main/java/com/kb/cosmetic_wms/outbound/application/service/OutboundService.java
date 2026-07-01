package com.kb.cosmetic_wms.outbound.application.service;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundAllocatedEvent;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundCanceledEvent;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundShippedEvent;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundStockReleaseRequestedEvent;
import com.kb.cosmetic_wms.outbound.application.port.in.CreateOutboundCommand;
import com.kb.cosmetic_wms.outbound.application.port.in.OutboundLifecycleUseCase;
import com.kb.cosmetic_wms.outbound.application.port.in.OutboundResult;
import com.kb.cosmetic_wms.outbound.application.port.out.OutboundPort;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundNotFoundException;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OutboundService implements OutboundLifecycleUseCase {

    private final OutboundPort outboundPort;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public OutboundResult createOutbound(CreateOutboundCommand command) {
        Outbound outbound = Outbound.create(
                command.ordersId(), command.warehouseId(), command.outboundType(), command.lines());
        return OutboundResult.from(outboundPort.save(outbound));
    }

    @Override
    @Transactional
    public OutboundResult allocateInventory(Long outboundId) {
        Outbound outbound = findByIdWithItemsForUpdateOrThrow(outboundId);
        outbound.allocate();
        Outbound saved = outboundPort.save(outbound);
        eventPublisher.publish(toAllocatedEvent(saved));
        return OutboundResult.from(saved);
    }

    @Override
    @Transactional
    public OutboundResult startProcessing(Long outboundId) {
        Outbound outbound = findByIdForUpdateOrThrow(outboundId);
        outbound.startProcessing();
        return OutboundResult.from(outboundPort.save(outbound));
    }

    @Override
    @Transactional
    public OutboundResult ship(Long outboundId) {
        Outbound outbound = findByIdForUpdateOrThrow(outboundId);
        outbound.ship(LocalDateTime.now());
        Outbound saved = outboundPort.save(outbound);
        eventPublisher.publish(new OutboundShippedEvent(saved.getId(), saved.getOrdersId()));
        return OutboundResult.from(saved);
    }

    @Override
    @Transactional
    public OutboundResult cancel(Long outboundId) {
        Outbound outbound = findByIdForUpdateOrThrow(outboundId);
        boolean wasAllocated = outbound.cancel();
        Outbound saved = outboundPort.save(outbound);
        eventPublisher.publish(new OutboundCanceledEvent(saved.getId(), saved.getOrdersId()));
        if (wasAllocated) {
            eventPublisher.publish(new OutboundStockReleaseRequestedEvent(saved.getId(), saved.getOrdersId()));
        }
        return OutboundResult.from(saved);
    }

    private Outbound findByIdForUpdateOrThrow(Long outboundId) {
        return outboundPort.findByIdForUpdate(outboundId)
                .orElseThrow(OutboundNotFoundException::new);
    }

    private Outbound findByIdWithItemsForUpdateOrThrow(Long outboundId) {
        return outboundPort.findByIdWithItemsForUpdate(outboundId)
                .orElseThrow(OutboundNotFoundException::new);
    }

    private OutboundAllocatedEvent toAllocatedEvent(Outbound outbound) {
        var snapshots = outbound.getOutboundItems().stream()
                .map(item -> new OutboundAllocatedEvent.ItemSnapshot(item.getInventoryId(), item.getTargetQuantity()))
                .toList();
        return new OutboundAllocatedEvent(outbound.getId(), outbound.getOrdersId(), snapshots);
    }
}