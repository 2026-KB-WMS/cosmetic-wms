package com.kb.cosmetic_wms.inspection.adapter.in.web;

import com.kb.cosmetic_wms.inspection.application.port.in.FindInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.InspectionLifecycleUseCase;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quality-inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final FindInspectionUseCase findInspectionUseCase;
    private final InspectionLifecycleUseCase inspectionLifecycleUseCase;

    @GetMapping("/{inspectionId}")
    public ResponseEntity<InspectionDetailResponse> getInspection(@PathVariable Long inspectionId) {
        return ResponseEntity.ok(InspectionDetailResponse.from(findInspectionUseCase.findById(inspectionId)));
    }

    @GetMapping
    public ResponseEntity<InspectionDetailResponse> getInspectionBySource(
            @RequestParam InspectionSourceType sourceType,
            @RequestParam Long sourceId) {
        return findInspectionUseCase.findBySource(sourceType, sourceId)
                .map(InspectionDetailResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{inspectionId}/start")
    public ResponseEntity<InspectionDetailResponse> startInspection(
            @PathVariable Long inspectionId,
            @Valid @RequestBody InspectionStartRequest request) {
        return ResponseEntity.ok(
                InspectionDetailResponse.from(inspectionLifecycleUseCase.start(inspectionId, request.inspectorId())));
    }

    @PatchMapping("/{inspectionId}/complete")
    public ResponseEntity<InspectionDetailResponse> completeInspection(
            @PathVariable Long inspectionId,
            @Valid @RequestBody InspectionCompleteRequest request) {
        return ResponseEntity.ok(
                InspectionDetailResponse.from(inspectionLifecycleUseCase.complete(
                        inspectionId, request.passedQuantity(), request.failedQuantity(), request.defectReason())));
    }
}