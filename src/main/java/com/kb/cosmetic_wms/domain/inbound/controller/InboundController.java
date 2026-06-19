package com.kb.cosmetic_wms.domain.inbound.controller;

import com.kb.cosmetic_wms.domain.inbound.dto.InboundCreateRequestDto;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundDetailResponseDto;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundItemAddRequestDto;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundItemResponseDto;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundPutawayRequestDto;
import com.kb.cosmetic_wms.domain.inbound.service.InboundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inbounds")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @PostMapping
    public ResponseEntity<InboundDetailResponseDto> registerInbound(
            @Valid @RequestBody InboundCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inboundService.registerInbound(request));
    }

    @GetMapping("/{inboundId}")
    public ResponseEntity<InboundDetailResponseDto> getInbound(
            @PathVariable("inboundId") Long inboundId) {
        return ResponseEntity.ok(inboundService.getInbound(inboundId));
    }

    @PostMapping("/{inboundId}/items")
    public ResponseEntity<InboundDetailResponseDto> addItem(
            @PathVariable("inboundId") Long inboundId,
            @Valid @RequestBody InboundItemAddRequestDto request) {
        return ResponseEntity.ok(inboundService.addItem(inboundId, request));
    }

    @PatchMapping("/{inboundId}/start")
    public ResponseEntity<InboundDetailResponseDto> startInbound(
            @PathVariable("inboundId") Long inboundId) {
        return ResponseEntity.ok(inboundService.startInbound(inboundId));
    }

    @PatchMapping("/{inboundId}/complete")
    public ResponseEntity<InboundDetailResponseDto> completeInbound(
            @PathVariable("inboundId") Long inboundId) {
        return ResponseEntity.ok(inboundService.completeInbound(inboundId));
    }

    @PatchMapping("/{inboundId}/cancel")
    public ResponseEntity<InboundDetailResponseDto> cancelInbound(
            @PathVariable("inboundId") Long inboundId) {
        return ResponseEntity.ok(inboundService.cancelInbound(inboundId));
    }

    @PatchMapping("/{inboundId}/items/{itemId}/putaway")
    public ResponseEntity<InboundItemResponseDto> completePutaway(
            @PathVariable("inboundId") Long inboundId,
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody InboundPutawayRequestDto request) {
        return ResponseEntity.ok(inboundService.completePutaway(inboundId, itemId, request));
    }

    @PatchMapping("/{inboundId}/items/{itemId}/approve")
    public ResponseEntity<InboundItemResponseDto> approveItem(
            @PathVariable("inboundId") Long inboundId,
            @PathVariable("itemId") Long itemId) {
        return ResponseEntity.ok(inboundService.approveItem(inboundId, itemId));
    }

    @PatchMapping("/{inboundId}/items/{itemId}/hold")
    public ResponseEntity<InboundItemResponseDto> holdItem(
            @PathVariable("inboundId") Long inboundId,
            @PathVariable("itemId") Long itemId) {
        return ResponseEntity.ok(inboundService.holdItem(inboundId, itemId));
    }
}
