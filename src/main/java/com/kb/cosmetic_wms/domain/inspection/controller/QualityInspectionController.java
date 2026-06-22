package com.kb.cosmetic_wms.domain.inspection.controller;

import com.kb.cosmetic_wms.domain.inspection.dto.InspectionCompleteRequestDto;
import com.kb.cosmetic_wms.domain.inspection.dto.InspectionStartRequestDto;
import com.kb.cosmetic_wms.domain.inspection.dto.QualityInspectionDetailResponseDto;
import com.kb.cosmetic_wms.domain.inspection.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quality-inspections")
@RequiredArgsConstructor
public class QualityInspectionController {

    private final InspectionService inspectionService;

    @GetMapping("/{inspectionId}")
    public ResponseEntity<QualityInspectionDetailResponseDto> getInspection(
            @PathVariable Long inspectionId) {
        return ResponseEntity.ok(inspectionService.getInspection(inspectionId));
    }

    @PatchMapping("/{inspectionId}/start")
    public ResponseEntity<QualityInspectionDetailResponseDto> startInspection(
            @PathVariable Long inspectionId,
            @Valid @RequestBody InspectionStartRequestDto request) {
        return ResponseEntity.ok(inspectionService.startInspection(inspectionId, request.inspectorId()));
    }

    @PatchMapping("/{inspectionId}/complete")
    public ResponseEntity<QualityInspectionDetailResponseDto> completeInspection(
            @PathVariable Long inspectionId,
            @Valid @RequestBody InspectionCompleteRequestDto request) {
        return ResponseEntity.ok(inspectionService.completeInspection(
                inspectionId, request.passedQuantity(), request.failedQuantity(), request.defectReason()));
    }
}