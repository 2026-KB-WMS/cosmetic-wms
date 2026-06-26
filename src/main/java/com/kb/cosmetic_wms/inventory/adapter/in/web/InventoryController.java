package com.kb.cosmetic_wms.inventory.adapter.in.web;

import com.kb.cosmetic_wms.inventory.application.port.in.FindInventoryUseCase;
import com.kb.cosmetic_wms.inventory.application.port.in.ManageInventoryStatusUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final FindInventoryUseCase findInventoryUseCase;
    private final ManageInventoryStatusUseCase manageInventoryStatusUseCase;

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryDetailResponse> getInventory(
            @PathVariable("inventoryId") Long inventoryId) {
        return ResponseEntity.ok(InventoryDetailResponse.from(findInventoryUseCase.findById(inventoryId)));
    }

    @GetMapping(params = "lotId")
    public ResponseEntity<List<InventoryDetailResponse>> getInventoriesByLot(
            @RequestParam Long lotId) {
        return ResponseEntity.ok(findInventoryUseCase.findByLotId(lotId).stream()
                .map(InventoryDetailResponse::from).toList());
    }

    @GetMapping(params = "productId")
    public ResponseEntity<List<InventoryDetailResponse>> getInventoriesByProduct(
            @RequestParam Long productId) {
        return ResponseEntity.ok(findInventoryUseCase.findByProductId(productId).stream()
                .map(InventoryDetailResponse::from).toList());
    }

    @PatchMapping("/{inventoryId}/allocate")
    public ResponseEntity<InventoryDetailResponse> allocate(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequest request) {
        return ResponseEntity.ok(InventoryDetailResponse.from(
                manageInventoryStatusUseCase.allocate(inventoryId, request.toCommand())));
    }

    @PatchMapping("/{inventoryId}/unallocate")
    public ResponseEntity<InventoryDetailResponse> unallocate(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequest request) {
        return ResponseEntity.ok(InventoryDetailResponse.from(
                manageInventoryStatusUseCase.unallocate(inventoryId, request.toCommand())));
    }

    @PatchMapping("/{inventoryId}/move/start")
    public ResponseEntity<InventoryDetailResponse> startMoving(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequest request) {
        return ResponseEntity.ok(InventoryDetailResponse.from(
                manageInventoryStatusUseCase.startMoving(inventoryId, request.toCommand())));
    }

    @PatchMapping("/{inventoryId}/move/finish")
    public ResponseEntity<InventoryDetailResponse> finishMoving(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequest request) {
        return ResponseEntity.ok(InventoryDetailResponse.from(
                manageInventoryStatusUseCase.finishMoving(inventoryId, request.toCommand())));
    }

    @PatchMapping("/{inventoryId}/quality/inspect")
    public ResponseEntity<InventoryDetailResponse> startInspecting(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequest request) {
        return ResponseEntity.ok(InventoryDetailResponse.from(
                manageInventoryStatusUseCase.startInspecting(inventoryId, request.toCommand())));
    }

    @PatchMapping("/{inventoryId}/quality/restore")
    public ResponseEntity<InventoryDetailResponse> restoreToNormalQuality(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequest request) {
        return ResponseEntity.ok(InventoryDetailResponse.from(
                manageInventoryStatusUseCase.restoreToNormalQuality(inventoryId, request.toCommand())));
    }

    @PatchMapping("/{inventoryId}/quality/hold")
    public ResponseEntity<InventoryDetailResponse> holdForQualityIssue(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequest request) {
        return ResponseEntity.ok(InventoryDetailResponse.from(
                manageInventoryStatusUseCase.holdForQualityIssue(inventoryId, request.toCommand())));
    }

    @PatchMapping("/{inventoryId}/quality/discard")
    public ResponseEntity<InventoryDetailResponse> scheduleForDiscard(
            @PathVariable("inventoryId") Long inventoryId,
            @Valid @RequestBody InventoryStatusChangeRequest request) {
        return ResponseEntity.ok(InventoryDetailResponse.from(
                manageInventoryStatusUseCase.scheduleForDiscard(inventoryId, request.toCommand())));
    }
}