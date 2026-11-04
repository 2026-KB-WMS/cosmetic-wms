package com.kb.cosmetic_wms.inspection.application.service;

import com.kb.cosmetic_wms.global.transaction.NewTransactionRunner;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionCommand;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.FailedInspectionResult;
import com.kb.cosmetic_wms.inspection.application.port.in.FindFailedInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.ResolveFailedInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionCreationFailurePort;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionPort;
import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.exception.FailedInspectionNotFoundException;
import com.kb.cosmetic_wms.inspection.domain.model.FailedInspection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FailedInspectionService implements FindFailedInspectionUseCase, ResolveFailedInspectionUseCase {

    private final InspectionCreationFailurePort failurePort;
    private final InspectionPort inspectionPort;
    private final CreateInspectionUseCase createInspectionUseCase;
    private final NewTransactionRunner newTransactionRunner;

    @Override
    @Transactional(readOnly = true)
    public List<FailedInspectionResult> findByStatus(FailedEventStatus status) {
        return failurePort.findByStatus(status).stream()
                .map(FailedInspectionResult::from)
                .toList();
    }

    /**
     * 실패 기록 행을 잠가 같은 건의 동시 재처리를 직렬화하고, 전표 생성은 별도 트랜잭션에서 시도한다.
     * 시도가 롤백되어도 실패 기록(시도 횟수, 최근 오류)은 이 트랜잭션에서 남는다.
     */
    @Override
    @Transactional
    public FailedInspectionResult retry(Long failedEventId) {
        FailedInspection failed = failurePort.findByIdForUpdate(failedEventId)
                .orElseThrow(FailedInspectionNotFoundException::new);
        failed.assertPending();

        try {
            boolean alreadyCreated = inspectionPort
                    .findBySourceTypeAndSourceId(InspectionSourceType.INBOUND, failed.getLineId())
                    .isPresent();
            if (!alreadyCreated) {
                newTransactionRunner.run(() -> createInspectionUseCase.create(toCommand(failed)));
            }
            failed.markResolvedByRetry(LocalDateTime.now());
        } catch (RuntimeException e) {
            failed.recordRetryFailure(describe(e));
        }

        FailedInspection saved = failurePort.save(failed);
        logRetryResult(saved);
        return FailedInspectionResult.from(saved);
    }

    @Override
    @Transactional
    public FailedInspectionResult discard(Long failedEventId) {
        FailedInspection failed = failurePort.findByIdForUpdate(failedEventId)
                .orElseThrow(FailedInspectionNotFoundException::new);
        failed.discard(LocalDateTime.now());
        FailedInspection saved = failurePort.save(failed);
        log.info("검사 전표 생성 실패 건을 폐기했습니다. failedEventId={}, lineId={}, status={}",
                saved.getId(), saved.getLineId(), saved.getStatus());
        return FailedInspectionResult.from(saved);
    }

    private static void logRetryResult(FailedInspection saved) {
        if (saved.getStatus() == FailedEventStatus.RESOLVED) {
            log.info("검사 전표 생성 재처리에 성공했습니다. failedEventId={}, lineId={}, status={}, retryCount={}",
                    saved.getId(), saved.getLineId(), saved.getStatus(), saved.getRetryCount());
            return;
        }
        log.warn("검사 전표 생성 재처리에 실패했습니다. failedEventId={}, lineId={}, retryCount={}, 사유={}",
                saved.getId(), saved.getLineId(), saved.getRetryCount(), saved.getErrorMessage());
    }

    private static CreateInspectionCommand toCommand(FailedInspection failed) {
        return new CreateInspectionCommand(
                InspectionSourceType.INBOUND,
                failed.getLineId(),
                failed.getReceivedQuantity(),
                failed.getProductId(),
                failed.getInboundId(),
                failed.getManufacturerLotNumber(),
                failed.getWarehouseId(),
                failed.getExpirationDate()
        );
    }

    private static String describe(RuntimeException e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
