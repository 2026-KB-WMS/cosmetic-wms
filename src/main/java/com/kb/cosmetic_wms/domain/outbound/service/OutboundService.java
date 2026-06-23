package com.kb.cosmetic_wms.domain.outbound.service;

import com.kb.cosmetic_wms.domain.outbound.OutboundLine;
import com.kb.cosmetic_wms.domain.outbound.dto.OutboundResponseDto;
import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundType;
import com.kb.cosmetic_wms.domain.outbound.exception.OutboundNotFoundException;
import com.kb.cosmetic_wms.domain.outbound.repository.OutboundRepository;
import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.global.event.OutboundAllocatedEvent;
import com.kb.cosmetic_wms.global.event.OutboundCanceledEvent;
import com.kb.cosmetic_wms.global.event.OutboundShippedEvent;
import com.kb.cosmetic_wms.global.event.OutboundStockReleaseRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OutboundService {

    private final OutboundRepository outboundRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public OutboundResponseDto createOutbound(Long ordersId, Long warehouseId, OutboundType outboundType, List<OutboundLine> lines) {
        Outbound outbound = Outbound.create(ordersId, warehouseId, outboundType, lines);
        return OutboundResponseDto.from(outboundRepository.save(outbound));
    }

    @Transactional
    public OutboundResponseDto allocateInventory(Long outboundId) {
        Outbound outbound = findByIdWithItemsForUpdateOrThrow(outboundId);
        outbound.allocate();
        eventPublisher.publish(toAllocatedEvent(outbound));
        return OutboundResponseDto.from(outbound);
    }

    @Transactional
    public OutboundResponseDto startProcessing(Long outboundId) {
        Outbound outbound = findByIdForUpdateOrThrow(outboundId);
        outbound.startProcessing();
        return OutboundResponseDto.from(outbound);
    }

    @Transactional
    public OutboundResponseDto ship(Long outboundId) {
        Outbound outbound = findByIdForUpdateOrThrow(outboundId);
        outbound.ship(LocalDateTime.now());
        eventPublisher.publish(new OutboundShippedEvent(outbound.getId(), outbound.getOrdersId()));
        return OutboundResponseDto.from(outbound);
    }

    @Transactional
    public OutboundResponseDto cancel(Long outboundId) {
        Outbound outbound = findByIdForUpdateOrThrow(outboundId);
        boolean wasAllocated = outbound.cancel();
        eventPublisher.publish(new OutboundCanceledEvent(outbound.getId(), outbound.getOrdersId()));
        if (wasAllocated) {
            eventPublisher.publish(new OutboundStockReleaseRequestedEvent(outbound.getId(), outbound.getOrdersId()));
        }
        return OutboundResponseDto.from(outbound);
    }

    private OutboundAllocatedEvent toAllocatedEvent(Outbound outbound) {
        var snapshots = outbound.getOutboundItems().stream()
                .map(item -> new OutboundAllocatedEvent.ItemSnapshot(item.getInventoryId(), item.getTargetQuantity()))
                .toList();
        return new OutboundAllocatedEvent(outbound.getId(), outbound.getOrdersId(), snapshots);
    }

    private Outbound findByIdForUpdateOrThrow(Long outboundId) {
        return outboundRepository.findByIdForUpdate(outboundId)
                .orElseThrow(OutboundNotFoundException::new);
    }

    private Outbound findByIdWithItemsForUpdateOrThrow(Long outboundId) {
        return outboundRepository.findByIdWithItemsForUpdate(outboundId)
                .orElseThrow(OutboundNotFoundException::new);
    }
}