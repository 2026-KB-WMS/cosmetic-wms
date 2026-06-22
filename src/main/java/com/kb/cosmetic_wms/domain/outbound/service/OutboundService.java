package com.kb.cosmetic_wms.domain.outbound.service;

import com.kb.cosmetic_wms.domain.outbound.OutboundLine;
import com.kb.cosmetic_wms.domain.outbound.dto.OutboundResponseDto;
import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundStatus;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundType;
import com.kb.cosmetic_wms.domain.outbound.event.OutboundAllocatedEvent;
import com.kb.cosmetic_wms.domain.outbound.event.OutboundCanceledEvent;
import com.kb.cosmetic_wms.domain.outbound.event.OutboundShippedEvent;
import com.kb.cosmetic_wms.domain.outbound.exception.OutboundNotFoundException;
import com.kb.cosmetic_wms.domain.outbound.repository.OutboundRepository;
import com.kb.cosmetic_wms.global.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OutboundService {

    private final OutboundRepository outboundRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public OutboundResponseDto createOutbound(Long ordersId, Long warehouseId, OutboundType outboundType, List<OutboundLine> lines) {
        Outbound outbound = Outbound.create(ordersId, warehouseId, outboundType);
        lines.forEach(outbound::addItem);
        return OutboundResponseDto.from(outboundRepository.save(outbound));
    }

    @Transactional
    public OutboundResponseDto allocateInventory(Long outboundId) {
        Outbound outbound = findByIdForUpdateOrThrow(outboundId);
        outbound.allocate();
        eventPublisher.publish(OutboundAllocatedEvent.from(outbound));
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
        outbound.ship();
        eventPublisher.publish(OutboundShippedEvent.from(outbound));
        return OutboundResponseDto.from(outbound);
    }

    @Transactional
    public OutboundResponseDto cancel(Long outboundId) {
        Outbound outbound = findByIdForUpdateOrThrow(outboundId);
        boolean wasAllocated = outbound.getOutboundStatus() == OutboundStatus.ALLOCATED;
        outbound.cancel();
        if (wasAllocated) {
            eventPublisher.publish(OutboundCanceledEvent.from(outbound));
        }
        return OutboundResponseDto.from(outbound);
    }

    private Outbound findByIdForUpdateOrThrow(Long outboundId) {
        return outboundRepository.findByIdForUpdate(outboundId)
                .orElseThrow(OutboundNotFoundException::new);
    }
}