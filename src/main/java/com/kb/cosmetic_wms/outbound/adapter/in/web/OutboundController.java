package com.kb.cosmetic_wms.outbound.adapter.in.web;

import com.kb.cosmetic_wms.outbound.application.port.in.FindOutboundUseCase;
import com.kb.cosmetic_wms.outbound.application.port.in.OutboundLifecycleUseCase;
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

    private final OutboundLifecycleUseCase outboundLifecycleUseCase;
    private final FindOutboundUseCase findOutboundUseCase;

    @PostMapping
    public ResponseEntity<OutboundResponse> createOutbound(
            @Valid @RequestBody CreateOutboundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OutboundResponse.from(outboundLifecycleUseCase.createOutbound(request.toCommand())));
    }

    @GetMapping
    public ResponseEntity<List<OutboundResponse>> getInProgressOutboundsByLot(@RequestParam("lotId") Long lotId) {
        return ResponseEntity.ok(findOutboundUseCase.findInProgressByLotId(lotId).stream()
                .map(OutboundResponse::from)
                .toList());
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