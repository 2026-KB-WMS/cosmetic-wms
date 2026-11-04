package com.kb.cosmetic_wms.oms.application.service;

import com.kb.cosmetic_wms.oms.application.port.in.RecordAssignmentFailureCommand;
import com.kb.cosmetic_wms.oms.application.port.in.RecordAssignmentFailureUseCase;
import com.kb.cosmetic_wms.oms.application.port.out.AssignmentFailurePort;
import com.kb.cosmetic_wms.oms.domain.model.FailedAssignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentFailureService implements RecordAssignmentFailureUseCase {

    private final AssignmentFailurePort assignmentFailurePort;

    /**
     * 실패 이력은 원 트랜잭션 롤백과 무관하게 반드시 남아야 하므로 REQUIRES_NEW로 격리한다.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(RecordAssignmentFailureCommand command) {
        assignmentFailurePort.save(FailedAssignment.record(
                command.orderId(), command.storeId(), command.errorMessage(), LocalDateTime.now()));
        log.warn("창고 배정 실패를 기록했습니다. orderId={}, storeId={}, 사유={}",
                command.orderId(), command.storeId(), command.errorMessage());
    }
}
