package com.kb.cosmetic_wms.inbound.adapter.in.web;

import com.kb.cosmetic_wms.inbound.application.port.in.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inbounds")
@RequiredArgsConstructor
public class InboundController {

    private final InboundLifecycleUseCase inboundLifecycleUseCase;
    private final FindInboundUseCase findInboundUseCase;

    @PostMapping
    public ResponseEntity<InboundDetailResponse> registerInbound(
            @Valid @RequestBody InboundCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InboundDetailResponse.from(inboundLifecycleUseCase.register(request.toCommand())));
    }

    @GetMapping("/{inboundId}")
    public ResponseEntity<InboundDetailResponse> getInbound(@PathVariable Long inboundId) {
        return ResponseEntity.ok(InboundDetailResponse.from(findInboundUseCase.findById(inboundId)));
    }

    @PatchMapping("/{inboundId}/receive")
    public ResponseEntity<InboundDetailResponse> receiveInbound(
            @PathVariable Long inboundId,
            @Valid @RequestBody InboundReceiveRequest request) {
        return ResponseEntity.ok(InboundDetailResponse.from(
                inboundLifecycleUseCase.receive(inboundId, request.toCommand())));
    }

    @PatchMapping("/{inboundId}/cancel")
    public ResponseEntity<InboundDetailResponse> cancelInbound(@PathVariable Long inboundId) {
        return ResponseEntity.ok(InboundDetailResponse.from(inboundLifecycleUseCase.cancel(inboundId)));
    }
}
