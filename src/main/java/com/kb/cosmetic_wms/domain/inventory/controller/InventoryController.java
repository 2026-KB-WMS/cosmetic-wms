package com.kb.cosmetic_wms.domain.inventory.controller;

import com.kb.cosmetic_wms.domain.inventory.dto.InventoryDetailResponseDto;
import com.kb.cosmetic_wms.domain.inventory.dto.InventoryStatusChangeRequestDto;
import com.kb.cosmetic_wms.domain.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryDetailResponseDto> getInventory(
            @PathVariable("inventoryId") Long inventoryId) {
        return ResponseEntity.ok(inventoryService.getInventory(inventoryId));
    }

    @GetMapping
    public ResponseEntity<List<InventoryDetailResponseDto>> getInventories(
            @RequestParam(value = "lotId", required = false) Long lotId,
            @RequestParam(value = "productId", required = false) Long productId) {
        if (lotId != null) {
            return ResponseEntity.ok(inventoryService.getInventoriesByLotId(lotId));
        }
        if (productId != null) {
            return ResponseEntity.ok(inventoryService.getInventoriesByProductId(productId));
        }
        return ResponseEntity.badRequest().build();
    }

    @PatchMapping("/{inventoryId}/allocate")
    public ResponseEntity<InventoryDetailResponseDto> allocate(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequestDto request) {
        return ResponseEntity.ok(inventoryService.allocate(inventoryId, request));
    }

    @PatchMapping("/{inventoryId}/unallocate")
    public ResponseEntity<InventoryDetailResponseDto> unallocate(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequestDto request) {
        return ResponseEntity.ok(inventoryService.unallocate(inventoryId, request));
    }

    @PatchMapping("/{inventoryId}/move/start")
    public ResponseEntity<InventoryDetailResponseDto> startMoving(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequestDto request) {
        return ResponseEntity.ok(inventoryService.startMoving(inventoryId, request));
    }

    @PatchMapping("/{inventoryId}/move/finish")
    public ResponseEntity<InventoryDetailResponseDto> finishMoving(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequestDto request) {
        return ResponseEntity.ok(inventoryService.finishMoving(inventoryId, request));
    }

    @PatchMapping("/{inventoryId}/quality/inspect")
    public ResponseEntity<InventoryDetailResponseDto> startInspecting(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequestDto request) {
        return ResponseEntity.ok(inventoryService.startInspecting(inventoryId, request));
    }

    @PatchMapping("/{inventoryId}/quality/restore")
    public ResponseEntity<InventoryDetailResponseDto> restoreToNormalQuality(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequestDto request) {
        return ResponseEntity.ok(inventoryService.restoreToNormalQuality(inventoryId, request));
    }

    @PatchMapping("/{inventoryId}/quality/hold")
    public ResponseEntity<InventoryDetailResponseDto> holdForQualityIssue(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequestDto request) {
        return ResponseEntity.ok(inventoryService.holdForQualityIssue(inventoryId, request));
    }

    @PatchMapping("/{inventoryId}/quality/discard")
    public ResponseEntity<InventoryDetailResponseDto> scheduleForDiscard(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequestDto request) {
        return ResponseEntity.ok(inventoryService.scheduleForDiscard(inventoryId, request));
    }
}
