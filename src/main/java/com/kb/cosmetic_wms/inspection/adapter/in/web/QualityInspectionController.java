package com.kb.cosmetic_wms.inspection.adapter.in.web;

import com.kb.cosmetic_wms.inspection.application.port.in.FindInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.InspectionLifecycleUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quality-inspections")
@RequiredArgsConstructor
public class QualityInspectionController {

    private final FindInspectionUseCase findInspectionUseCase;
    private final InspectionLifecycleUseCase inspectionLifecycleUseCase;

    @GetMapping("/{inspectionId}")
    public ResponseEntity<InspectionDetailResponse> getInspection(@PathVariable Long inspectionId) {
        return ResponseEntity.ok(InspectionDetailResponse.from(findInspectionUseCase.findById(inspectionId)));
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