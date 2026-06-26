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
    private final InboundItemUseCase inboundItemUseCase;
    private final FindInboundUseCase findInboundUseCase;

    @PostMapping
    public ResponseEntity<InboundDetailResponse> registerInbound(
            @Valid @RequestBody InboundCreateRequest request) {
        RegisterInboundCommand command = new RegisterInboundCommand(
                request.warehouseId(), request.partnerId(), request.inboundDate());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InboundDetailResponse.from(inboundLifecycleUseCase.register(command)));
    }

    @GetMapping("/{inboundId}")
    public ResponseEntity<InboundDetailResponse> getInbound(@PathVariable Long inboundId) {
        return ResponseEntity.ok(InboundDetailResponse.from(findInboundUseCase.findById(inboundId)));
    }

    @PostMapping("/{inboundId}/items")
    public ResponseEntity<InboundDetailResponse> addItem(
            @PathVariable Long inboundId,
            @Valid @RequestBody InboundItemAddRequest request) {
        AddInboundItemCommand command = new AddInboundItemCommand(
                request.productId(), request.quantity(),
                request.manufactureDate(), request.expirationDate());
        return ResponseEntity.ok(InboundDetailResponse.from(inboundItemUseCase.addItem(inboundId, command)));
    }

    @PatchMapping("/{inboundId}/start")
    public ResponseEntity<InboundDetailResponse> startInbound(@PathVariable Long inboundId) {
        return ResponseEntity.ok(InboundDetailResponse.from(inboundLifecycleUseCase.start(inboundId)));
    }

    @PatchMapping("/{inboundId}/complete")
    public ResponseEntity<InboundDetailResponse> completeInbound(@PathVariable Long inboundId) {
        return ResponseEntity.ok(InboundDetailResponse.from(inboundLifecycleUseCase.complete(inboundId)));
    }

    @PatchMapping("/{inboundId}/cancel")
    public ResponseEntity<InboundDetailResponse> cancelInbound(@PathVariable Long inboundId) {
        return ResponseEntity.ok(InboundDetailResponse.from(inboundLifecycleUseCase.cancel(inboundId)));
    }

    @PatchMapping("/{inboundId}/items/{itemId}/putaway")
    public ResponseEntity<InboundItemResponse> completePutaway(
            @PathVariable Long inboundId,
            @PathVariable Long itemId,
            @Valid @RequestBody InboundPutawayRequest request) {
        PutawayCommand command = new PutawayCommand(request.lotId(), request.sectionId());
        return ResponseEntity.ok(InboundItemResponse.from(inboundItemUseCase.completePutaway(inboundId, itemId, command)));
    }

    @PatchMapping("/{inboundId}/items/{itemId}/approve")
    public ResponseEntity<InboundItemResponse> approveItem(
            @PathVariable Long inboundId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(InboundItemResponse.from(inboundItemUseCase.approve(inboundId, itemId)));
    }

    @PatchMapping("/{inboundId}/items/{itemId}/hold")
    public ResponseEntity<InboundItemResponse> holdItem(
            @PathVariable Long inboundId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(InboundItemResponse.from(inboundItemUseCase.hold(inboundId, itemId)));
    }
}
