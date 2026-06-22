package com.kb.cosmetic_wms.domain.outbound.controller;

import com.kb.cosmetic_wms.domain.outbound.OutboundLine;
import com.kb.cosmetic_wms.domain.outbound.dto.CreateOutboundRequestDto;
import com.kb.cosmetic_wms.domain.outbound.dto.OutboundResponseDto;
import com.kb.cosmetic_wms.domain.outbound.service.OutboundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/outbounds")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @PostMapping
    public ResponseEntity<OutboundResponseDto> createOutbound(
            @Valid @RequestBody CreateOutboundRequestDto request) {
        List<OutboundLine> lines = request.items().stream()
                .map(item -> new OutboundLine(item.orderItemId(), item.inventoryId(), item.targetQuantity()))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(outboundService.createOutbound(
                        request.ordersId(), request.warehouseId(), request.outboundType(), lines));
    }

    @PatchMapping("/{outboundId}/allocate")
    public ResponseEntity<OutboundResponseDto> allocateInventory(
            @PathVariable("outboundId") Long outboundId) {
        return ResponseEntity.ok(outboundService.allocateInventory(outboundId));
    }

    @PatchMapping("/{outboundId}/process")
    public ResponseEntity<OutboundResponseDto> startProcessing(
            @PathVariable("outboundId") Long outboundId) {
        return ResponseEntity.ok(outboundService.startProcessing(outboundId));
    }

    @PatchMapping("/{outboundId}/ship")
    public ResponseEntity<OutboundResponseDto> ship(
            @PathVariable("outboundId") Long outboundId) {
        return ResponseEntity.ok(outboundService.ship(outboundId));
    }

    @PatchMapping("/{outboundId}/cancel")
    public ResponseEntity<OutboundResponseDto> cancel(
            @PathVariable("outboundId") Long outboundId) {
        return ResponseEntity.ok(outboundService.cancel(outboundId));
    }
}