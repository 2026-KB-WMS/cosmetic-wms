package com.kb.cosmetic_wms.domain.lot.controller;

import com.kb.cosmetic_wms.domain.lot.dto.LotCreateRequestDto;
import com.kb.cosmetic_wms.domain.lot.dto.LotDetailResponseDto;
import com.kb.cosmetic_wms.domain.lot.dto.LotStatusUpdateRequestDto;
import com.kb.cosmetic_wms.domain.lot.service.LotService;
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

    private final LotService lotService;

    @PostMapping
    public ResponseEntity<LotDetailResponseDto> register(
            @Valid @RequestBody LotCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lotService.register(request));
    }

    @GetMapping("/{lotId}")
    public ResponseEntity<LotDetailResponseDto> getLot(
            @PathVariable("lotId") Long lotId) {
        return ResponseEntity.ok(lotService.getLot(lotId));
    }

    @GetMapping
    public ResponseEntity<List<LotDetailResponseDto>> getLotsByProductId(
            @RequestParam("productId") Long productId) {
        return ResponseEntity.ok(lotService.getLotsByProductId(productId));
    }

    @PatchMapping("/{lotId}/status")
    public ResponseEntity<LotDetailResponseDto> updateLotStatus(
            @PathVariable("lotId") Long lotId,
            @Valid @RequestBody LotStatusUpdateRequestDto request) {
        return ResponseEntity.ok(lotService.updateLotStatus(lotId, request));
    }

    @DeleteMapping("/{lotId}")
    public ResponseEntity<Void> deleteLot(
            @PathVariable("lotId") Long lotId) {
        lotService.deleteLot(lotId);
        return ResponseEntity.noContent().build();
    }
}
