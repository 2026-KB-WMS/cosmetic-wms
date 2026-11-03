package com.kb.cosmetic_wms.inspection.adapter.in.web;

import com.kb.cosmetic_wms.inspection.application.port.in.FindFailedInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.ResolveFailedInspectionUseCase;
import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/failed-inspections")
@RequiredArgsConstructor
public class FailedInspectionController {

    private final FindFailedInspectionUseCase findFailedInspectionUseCase;
    private final ResolveFailedInspectionUseCase resolveFailedInspectionUseCase;

    @GetMapping
    public ResponseEntity<List<FailedInspectionResponse>> getFailedInspections(
            @RequestParam(defaultValue = "PENDING") FailedEventStatus status) {
        return ResponseEntity.ok(findFailedInspectionUseCase.findByStatus(status).stream()
                .map(FailedInspectionResponse::from)
                .toList());
    }

    @PostMapping("/{failedEventId}/retry")
    public ResponseEntity<FailedInspectionResponse> retry(@PathVariable Long failedEventId) {
        return ResponseEntity.ok(FailedInspectionResponse.from(resolveFailedInspectionUseCase.retry(failedEventId)));
    }

    @PostMapping("/{failedEventId}/discard")
    public ResponseEntity<FailedInspectionResponse> discard(@PathVariable Long failedEventId) {
        return ResponseEntity.ok(FailedInspectionResponse.from(resolveFailedInspectionUseCase.discard(failedEventId)));
    }
}
