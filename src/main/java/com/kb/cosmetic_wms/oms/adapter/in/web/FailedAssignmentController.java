package com.kb.cosmetic_wms.oms.adapter.in.web;

import com.kb.cosmetic_wms.oms.application.port.in.FindFailedAssignmentUseCase;
import com.kb.cosmetic_wms.oms.application.port.in.ResolveFailedAssignmentUseCase;
import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
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
@RequestMapping("/api/v1/admin/failed-assignments")
@RequiredArgsConstructor
public class FailedAssignmentController {

    private final FindFailedAssignmentUseCase findFailedAssignmentUseCase;
    private final ResolveFailedAssignmentUseCase resolveFailedAssignmentUseCase;

    @GetMapping
    public ResponseEntity<List<FailedAssignmentResponse>> getFailedAssignments(
            @RequestParam(defaultValue = "PENDING") FailedEventStatus status) {
        return ResponseEntity.ok(findFailedAssignmentUseCase.findByStatus(status).stream()
                .map(FailedAssignmentResponse::from)
                .toList());
    }

    @PostMapping("/{failedEventId}/retry")
    public ResponseEntity<FailedAssignmentResponse> retry(@PathVariable Long failedEventId) {
        return ResponseEntity.ok(FailedAssignmentResponse.from(resolveFailedAssignmentUseCase.retry(failedEventId)));
    }

    @PostMapping("/{failedEventId}/discard")
    public ResponseEntity<FailedAssignmentResponse> discard(@PathVariable Long failedEventId) {
        return ResponseEntity.ok(FailedAssignmentResponse.from(resolveFailedAssignmentUseCase.discard(failedEventId)));
    }
}
