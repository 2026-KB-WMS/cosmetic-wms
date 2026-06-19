package com.kb.cosmetic_wms.domain.inbound.service;

import com.kb.cosmetic_wms.domain.inbound.InboundLine;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundCreateRequestDto;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundDetailResponseDto;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundItemAddRequestDto;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundItemResponseDto;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundPutawayRequestDto;
import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.entity.InboundItem;
import com.kb.cosmetic_wms.domain.inbound.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundEmptyItemsException;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundNotFoundException;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundProductNotFoundException;
import com.kb.cosmetic_wms.domain.inbound.repository.InboundRepository;
import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerNotFoundException;
import com.kb.cosmetic_wms.domain.partner.repository.PartnerRepository;
import com.kb.cosmetic_wms.domain.product.repository.ProductRepository;
import com.kb.cosmetic_wms.domain.storage.exception.WarehouseNotFoundException;
import com.kb.cosmetic_wms.domain.storage.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InboundService {

    private final InboundRepository inboundRepository;
    private final WarehouseRepository warehouseRepository;
    private final PartnerRepository partnerRepository;
    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public InboundDetailResponseDto registerInbound(InboundCreateRequestDto request) {
        warehouseRepository.findById(request.warehouseId())
                .orElseThrow(WarehouseNotFoundException::new);
        partnerRepository.findById(request.partnerId())
                .orElseThrow(PartnerNotFoundException::new);

        Inbound inbound = Inbound.create(request.inboundDate(), request.warehouseId(), request.partnerId());
        return InboundDetailResponseDto.from(inboundRepository.save(inbound));
    }

    @Transactional
    public InboundDetailResponseDto addItem(Long inboundId, InboundItemAddRequestDto request) {
        Inbound inbound = findInboundWithItemsOrThrow(inboundId);
        productRepository.findById(request.productId())
                .orElseThrow(InboundProductNotFoundException::new);

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
        eventPublisher.publish(InboundCompletedEvent.from(inbound));
        return InboundDetailResponseDto.from(inbound);
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
