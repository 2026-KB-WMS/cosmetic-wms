package com.kb.cosmetic_wms.oms.application.service;

import com.kb.cosmetic_wms.global.transaction.NewTransactionRunner;
import com.kb.cosmetic_wms.oms.application.port.in.AssignWarehouseCommand;
import com.kb.cosmetic_wms.oms.application.port.in.AssignWarehouseUseCase;
import com.kb.cosmetic_wms.oms.application.port.in.FailedAssignmentResult;
import com.kb.cosmetic_wms.oms.application.port.in.FindFailedAssignmentUseCase;
import com.kb.cosmetic_wms.oms.application.port.in.ResolveFailedAssignmentUseCase;
import com.kb.cosmetic_wms.oms.application.port.out.AssignmentFailurePort;
import com.kb.cosmetic_wms.oms.application.port.out.LoadOrderForAssignmentPort;
import com.kb.cosmetic_wms.oms.application.port.out.LoadOrderForAssignmentPort.OrderForAssignment;
import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.exception.AssignmentOrderNotFoundException;
import com.kb.cosmetic_wms.oms.domain.exception.FailedAssignmentNotFoundException;
import com.kb.cosmetic_wms.oms.domain.exception.OrderNotAwaitingAssignmentException;
import com.kb.cosmetic_wms.oms.domain.model.FailedAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FailedAssignmentService implements FindFailedAssignmentUseCase, ResolveFailedAssignmentUseCase {

    private final AssignmentFailurePort failurePort;
    private final LoadOrderForAssignmentPort loadOrderForAssignmentPort;
    private final AssignWarehouseUseCase assignWarehouseUseCase;
    private final NewTransactionRunner newTransactionRunner;

    @Override
    @Transactional(readOnly = true)
    public List<FailedAssignmentResult> findByStatus(FailedEventStatus status) {
        return failurePort.findByStatus(status).stream()
                .map(FailedAssignmentResult::from)
                .toList();
    }

    /**
     * 실패 기록 행을 잠가 같은 건의 동시 재처리를 직렬화하고, 배정은 별도 트랜잭션에서 시도한다.
     * 시도가 롤백되어도 실패 기록(시도 횟수, 최근 오류)은 이 트랜잭션에서 남는다.
     * 발주가 더 이상 배정 대기 상태가 아니면 외부 Routing API를 호출하기 전에 사유를 남기고 끝낸다.
     */
    @Override
    @Transactional
    public FailedAssignmentResult retry(Long failedEventId) {
        FailedAssignment failed = failurePort.findByIdForUpdate(failedEventId)
                .orElseThrow(FailedAssignmentNotFoundException::new);
        failed.assertPending();

        try {
            OrderForAssignment order = loadOrderForAssignmentPort.load(failed.getOrderId())
                    .orElseThrow(AssignmentOrderNotFoundException::new);
            if (!order.awaitingAssignment()) {
                throw new OrderNotAwaitingAssignmentException();
            }
            newTransactionRunner.run(() -> assignWarehouseUseCase.assign(toCommand(order)));
            failed.markResolvedByRetry(LocalDateTime.now());
        } catch (RuntimeException e) {
            failed.recordRetryFailure(describe(e));
        }

        return FailedAssignmentResult.from(failurePort.save(failed));
    }

    @Override
    @Transactional
    public FailedAssignmentResult discard(Long failedEventId) {
        FailedAssignment failed = failurePort.findByIdForUpdate(failedEventId)
                .orElseThrow(FailedAssignmentNotFoundException::new);
        failed.discard(LocalDateTime.now());
        return FailedAssignmentResult.from(failurePort.save(failed));
    }

    private static AssignWarehouseCommand toCommand(OrderForAssignment order) {
        return new AssignWarehouseCommand(
                order.orderId(),
                order.storeId(),
                order.items().stream()
                        .map(item -> new AssignWarehouseCommand.ItemDemand(
                                item.orderItemId(), item.productId(), item.quantity()))
                        .toList()
        );
    }

    private static String describe(RuntimeException e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
