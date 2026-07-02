package com.kb.cosmetic_wms.inspection.application.service;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.inspection.domain.event.InspectionCompletedEvent;
import com.kb.cosmetic_wms.inspection.application.port.in.FindInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.InspectionLifecycleUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.InspectionResult;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionPort;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionNotFoundException;
import com.kb.cosmetic_wms.inspection.domain.model.Inspection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionService implements InspectionLifecycleUseCase, FindInspectionUseCase {

    private final InspectionPort inspectionPort;
    private final EventPublisher eventPublisher;

    @Override
    public InspectionResult findById(Long inspectionId) {
        return InspectionResult.from(findOrThrow(inspectionId));
    }

    @Override
    public Optional<InspectionResult> findBySource(InspectionSourceType sourceType, Long sourceId) {
        return inspectionPort.findBySourceTypeAndSourceId(sourceType, sourceId)
                .map(InspectionResult::from);
    }

    @Override
    @Transactional
    public InspectionResult start(Long inspectionId, Long inspectorId) {
        Inspection inspection = inspectionPort.findByIdForUpdate(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);
        inspection.startInspection(inspectorId);
        return InspectionResult.from(inspectionPort.save(inspection));
    }

    @Override
    @Transactional
    public InspectionResult complete(Long inspectionId, int passedQty, int failedQty, String defectReason) {
        Inspection inspection = inspectionPort.findByIdForUpdate(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);
        inspection.completeInspection(passedQty, failedQty, defectReason);
        Inspection saved = inspectionPort.save(inspection);
        eventPublisher.publish(toInspectionCompletedEvent(saved));
        return InspectionResult.from(saved);
    }

    private Inspection findOrThrow(Long inspectionId) {
        return inspectionPort.findById(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);
    }

    private InspectionCompletedEvent toInspectionCompletedEvent(Inspection inspection) {
        return new InspectionCompletedEvent(
                inspection.getId(),
                inspection.getSourceType().name(),
                inspection.getSourceId(),
                inspection.getProductId(),
                inspection.getLotId(),
                inspection.getWarehouseId(),
                inspection.getInspectionQuantity(),
                inspection.getPassedQuantity(),
                inspection.getFailedQuantity(),
                inspection.getDefectReason(),
                inspection.getExpiryDate()
        );
    }
}
