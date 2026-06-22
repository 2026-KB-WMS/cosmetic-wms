package com.kb.cosmetic_wms.domain.inspection.service;

import com.kb.cosmetic_wms.domain.inspection.entity.QualityInspection;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionSourceType;
import com.kb.cosmetic_wms.domain.inspection.event.InspectionCompletedEvent;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionNotFoundException;
import com.kb.cosmetic_wms.domain.inspection.repository.InspectionRepository;
import com.kb.cosmetic_wms.global.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public QualityInspection createInboundInspection(Long inboundItemId, int inspectionQuantity) {
        QualityInspection inspection = QualityInspection.createPending(
                InspectionSourceType.INBOUND, inboundItemId, null, inspectionQuantity);
        return inspectionRepository.save(inspection);
    }

    @Transactional
    public void startInspection(Long inspectionId, Long inspectorId) {
        QualityInspection inspection = inspectionRepository.findByIdForUpdate(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);
        inspection.startInspection(inspectorId);
    }

    @Transactional
    public void completeInspection(Long inspectionId, int passedQty, int failedQty, String defectReason) {
        QualityInspection inspection = inspectionRepository.findByIdForUpdate(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);
        inspection.completeInspection(passedQty, failedQty, defectReason);
        eventPublisher.publish(InspectionCompletedEvent.from(inspection));
    }
}
