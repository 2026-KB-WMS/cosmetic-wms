package com.kb.cosmetic_wms.lot.adapter.in.web;

import com.kb.cosmetic_wms.lot.application.port.in.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lots")
@RequiredArgsConstructor
public class LotController {

    private final RegisterLotUseCase registerLotUseCase;
    private final FindLotUseCase findLotUseCase;
    private final UpdateLotStatusUseCase updateLotStatusUseCase;
    private final DeleteLotUseCase deleteLotUseCase;

    @PostMapping
    public ResponseEntity<LotDetailResponse> register(
            @Valid @RequestBody RegisterLotRequest request) {
        LotResult result = registerLotUseCase.register(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(LotDetailResponse.from(result));
    }

    @GetMapping("/{lotId}")
    public ResponseEntity<LotDetailResponse> getLot(
            @PathVariable("lotId") Long lotId) {
        return ResponseEntity.ok(LotDetailResponse.from(findLotUseCase.findById(lotId)));
    }

    @GetMapping
    public ResponseEntity<List<LotDetailResponse>> getLotsByProductId(
            @RequestParam("productId") Long productId) {
        List<LotDetailResponse> responses = findLotUseCase.findByProductId(productId).stream()
                .map(LotDetailResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{lotId}/status")
    public ResponseEntity<LotDetailResponse> updateLotStatus(
            @PathVariable("lotId") Long lotId,
            @Valid @RequestBody UpdateLotStatusRequest request) {
        LotResult result = updateLotStatusUseCase.updateStatus(lotId, request.toCommand());
        return ResponseEntity.ok(LotDetailResponse.from(result));
    }

    @DeleteMapping("/{lotId}")
    public ResponseEntity<Void> deleteLot(
            @PathVariable("lotId") Long lotId) {
        deleteLotUseCase.delete(lotId);
        return ResponseEntity.noContent().build();
    }
}