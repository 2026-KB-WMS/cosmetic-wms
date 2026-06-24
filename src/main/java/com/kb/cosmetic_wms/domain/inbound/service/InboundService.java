package com.kb.cosmetic_wms.domain.inbound.service;

import com.kb.cosmetic_wms.domain.inbound.InboundLine;
import com.kb.cosmetic_wms.domain.inbound.dto.*;
import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundEmptyItemsException;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundNotFoundException;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundProductNotFoundException;
import com.kb.cosmetic_wms.domain.inbound.repository.InboundRepository;
import com.kb.cosmetic_wms.product.product.application.port.in.FindProductUseCase;
import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.global.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.partner.application.port.out.PartnerPort;
import com.kb.cosmetic_wms.partner.domain.exception.PartnerNotFoundException;
import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import com.kb.cosmetic_wms.storage.domain.exception.WarehouseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InboundService {

    private final InboundRepository inboundRepository;
    private final StoragePort storagePort;
    private final PartnerPort partnerPort;
    private final FindProductUseCase findProductUseCase;
    private final EventPublisher eventPublisher;

    @Transactional
    public InboundDetailResponseDto registerInbound(InboundCreateRequestDto request) {
        storagePort.findById(request.warehouseId())
                .orElseThrow(WarehouseNotFoundException::new);
        partnerPort.findById(request.partnerId())
                .orElseThrow(PartnerNotFoundException::new);

        Inbound inbound = Inbound.create(request.inboundDate(), request.warehouseId(), request.partnerId());
        return InboundDetailResponseDto.from(inboundRepository.save(inbound));
    }

    @Transactional
    public InboundDetailResponseDto addItem(Long inboundId, InboundItemAddRequestDto request) {
        Inbound inbound = findInboundWithItemsOrThrow(inboundId);
        if (!findProductUseCase.existsById(request.productId())) {
            throw new InboundProductNotFoundException();
        }

        InboundLine line = new InboundLine(
                request.productId(), request.quantity(),
                request.manufactureDate(), request.expirationDate()
        );
        inbound.addItem(line);
        return InboundDetailResponseDto.from(inbound);
    }

    @Transactional
    public InboundDetailResponseDto startInbound(Long inboundId) {
        Inbound inbound = findInboundWithItemsOrThrow(inboundId);
        if (inbound.getInboundItems().isEmpty()) {
            throw new InboundEmptyItemsException();
        }
        inbound.startExecution();
        return InboundDetailResponseDto.from(inbound);
    }

    @Transactional
    public InboundItemResponseDto completePutaway(Long inboundId, Long itemId, InboundPutawayRequestDto request) {
        Inbound inbound = findInboundWithItemsOrThrow(inboundId);
        return InboundItemResponseDto.from(inbound.putawayItem(itemId, request.lotId(), request.sectionId()));
    }

    @Transactional
    public InboundItemResponseDto approveItem(Long inboundId, Long itemId) {
        Inbound inbound = findInboundWithItemsOrThrow(inboundId);
        return InboundItemResponseDto.from(inbound.approveItem(itemId));
    }

    @Transactional
    public InboundItemResponseDto holdItem(Long inboundId, Long itemId) {
        Inbound inbound = findInboundWithItemsOrThrow(inboundId);
        return InboundItemResponseDto.from(inbound.holdItem(itemId));
    }

    @Transactional
    public InboundDetailResponseDto completeInbound(Long inboundId) {
        Inbound inbound = inboundRepository.findByIdWithItemsForUpdate(inboundId)
                .orElseThrow(InboundNotFoundException::new);
        inbound.completeExecution();
        eventPublisher.publish(toInboundCompletedEvent(inbound));
        return InboundDetailResponseDto.from(inbound);
    }

    private InboundCompletedEvent toInboundCompletedEvent(Inbound inbound) {
        var snapshots = inbound.getInboundItems().stream()
                .map(item -> new InboundCompletedEvent.ItemSnapshot(
                        item.getId(),
                        item.getProductId(),
                        item.getLotId(),
                        item.getSectionId(),
                        item.getQuantity()
                ))
                .toList();
        return new InboundCompletedEvent(inbound.getId(), inbound.getWarehouseId(), snapshots);
    }

    @Transactional
    public InboundDetailResponseDto cancelInbound(Long inboundId) {
        Inbound inbound = findInboundWithItemsOrThrow(inboundId);
        inbound.cancel();
        return InboundDetailResponseDto.from(inbound);
    }

    public InboundDetailResponseDto getInbound(Long inboundId) {
        return InboundDetailResponseDto.from(findInboundWithItemsOrThrow(inboundId));
    }

    private Inbound findInboundWithItemsOrThrow(Long inboundId) {
        return inboundRepository.findByIdWithItems(inboundId)
                .orElseThrow(InboundNotFoundException::new);
    }
}
