package com.kb.cosmetic_wms.outbound.adapter.in.web;

import com.kb.cosmetic_wms.outbound.application.port.in.OutboundLifecycleUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/outbounds")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundLifecycleUseCase outboundLifecycleUseCase;

    @PostMapping
    public ResponseEntity<OutboundResponse> createOutbound(
            @Valid @RequestBody CreateOutboundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OutboundResponse.from(outboundLifecycleUseCase.createOutbound(request.toCommand())));
    }

    @PatchMapping("/{outboundId}/allocate")
    public ResponseEntity<OutboundResponse> allocateInventory(
            @PathVariable("outboundId") Long outboundId) {
        return ResponseEntity.ok(OutboundResponse.from(outboundLifecycleUseCase.allocateInventory(outboundId)));
    }

    @PatchMapping("/{outboundId}/process")
    public ResponseEntity<OutboundResponse> startProcessing(
            @PathVariable("outboundId") Long outboundId) {
        return ResponseEntity.ok(OutboundResponse.from(outboundLifecycleUseCase.startProcessing(outboundId)));
    }

    @PatchMapping("/{outboundId}/ship")
    public ResponseEntity<OutboundResponse> ship(
            @PathVariable("outboundId") Long outboundId) {
        return ResponseEntity.ok(OutboundResponse.from(outboundLifecycleUseCase.ship(outboundId)));
    }

    @PatchMapping("/{outboundId}/cancel")
    public ResponseEntity<OutboundResponse> cancel(
            @PathVariable("outboundId") Long outboundId) {
        return ResponseEntity.ok(OutboundResponse.from(outboundLifecycleUseCase.cancel(outboundId)));
    }
}