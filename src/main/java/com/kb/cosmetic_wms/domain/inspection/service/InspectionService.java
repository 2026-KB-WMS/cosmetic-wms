package com.kb.cosmetic_wms.domain.inspection.service;

import com.kb.cosmetic_wms.domain.inspection.dto.QualityInspectionDetailResponseDto;
import com.kb.cosmetic_wms.domain.inspection.entity.QualityInspection;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionSourceType;
import java.time.LocalDate;
import com.kb.cosmetic_wms.global.event.InspectionCompletedEvent;
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
    public QualityInspection createInboundInspection(Long inboundItemId, Long productId, Long lotId,
                                                     Long sectionId, Long warehouseId, int inspectionQuantity,
                                                     LocalDate expiryDate) {
        QualityInspection inspection = QualityInspection.createPending(
                InspectionSourceType.INBOUND, inboundItemId, null, inspectionQuantity,
                productId, lotId, sectionId, warehouseId, expiryDate);
        return inspectionRepository.save(inspection);
    }

    public QualityInspectionDetailResponseDto getInspection(Long inspectionId) {
        QualityInspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);
        return QualityInspectionDetailResponseDto.from(inspection);
    }

    @Transactional
    public QualityInspectionDetailResponseDto startInspection(Long inspectionId, Long inspectorId) {
        QualityInspection inspection = inspectionRepository.findByIdForUpdate(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);
        inspection.startInspection(inspectorId);
        return QualityInspectionDetailResponseDto.from(inspection);
    }

    @Transactional
    public QualityInspectionDetailResponseDto completeInspection(Long inspectionId, int passedQty, int failedQty, String defectReason) {
        QualityInspection inspection = inspectionRepository.findByIdForUpdate(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);
        inspection.completeInspection(passedQty, failedQty, defectReason);
        eventPublisher.publish(new InspectionCompletedEvent(
                inspection.getId(),
                inspection.getSourceType().name(),
                inspection.getSourceId(),
                inspection.getProductId(),
                inspection.getLotId(),
                inspection.getSectionId(),
                inspection.getWarehouseId(),
                inspection.getInspectionQuantity(),
                inspection.getPassedQuantity(),
                inspection.getFailedQuantity(),
                inspection.getDefectReason(),
                inspection.getExpiryDate()
        ));
        return QualityInspectionDetailResponseDto.from(inspection);
    }
}
