package com.kb.cosmetic_wms.oms;

import com.kb.cosmetic_wms.global.transaction.NewTransactionRunner;
import com.kb.cosmetic_wms.oms.application.port.in.AssignWarehouseCommand;
import com.kb.cosmetic_wms.oms.application.port.in.AssignWarehouseUseCase;
import com.kb.cosmetic_wms.oms.application.port.in.FailedAssignmentResult;
import com.kb.cosmetic_wms.oms.application.port.in.WarehouseAssignmentResult;
import com.kb.cosmetic_wms.oms.application.port.out.AssignmentFailurePort;
import com.kb.cosmetic_wms.oms.application.port.out.LoadOrderForAssignmentPort;
import com.kb.cosmetic_wms.oms.application.port.out.LoadOrderForAssignmentPort.OrderForAssignment;
import com.kb.cosmetic_wms.oms.application.service.FailedAssignmentService;
import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.exception.FailedAssignmentNotFoundException;
import com.kb.cosmetic_wms.oms.domain.exception.FailedAssignmentNotPendingException;
import com.kb.cosmetic_wms.oms.domain.exception.NoAssignableWarehouseException;
import com.kb.cosmetic_wms.oms.domain.exception.OmsErrorCode;
import com.kb.cosmetic_wms.oms.domain.model.FailedAssignment;
import com.kb.cosmetic_wms.support.LogCapture;
import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FailedAssignmentServiceTest {

    @InjectMocks
    private FailedAssignmentService service;

    @Mock
    private AssignmentFailurePort failurePort;

    @Mock
    private LoadOrderForAssignmentPort loadOrderForAssignmentPort;

    @Mock
    private AssignWarehouseUseCase assignWarehouseUseCase;

    @Spy
    private NewTransactionRunner newTransactionRunner = new NewTransactionRunner();

    private FailedAssignment pending() {
        return FailedAssignment.reconstitute(1L, 100L, 10L, "Routing API timeout",
                LocalDateTime.of(2026, 11, 3, 12, 0), FailedEventStatus.PENDING, 0, null);
    }

    private OrderForAssignment order(boolean awaitingAssignment) {
        return new OrderForAssignment(100L, 10L, awaitingAssignment,
                List.of(new OrderForAssignment.Item(5L, 200L, 30)));
    }

    private void givenSaveReturnsArgument() {
        given(failurePort.save(any(FailedAssignment.class))).willAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void 상태별로_실패_건을_조회한다() {
        given(failurePort.findByStatus(FailedEventStatus.PENDING)).willReturn(List.of(pending()));

        List<FailedAssignmentResult> results = service.findByStatus(FailedEventStatus.PENDING);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).orderId()).isEqualTo(100L);
    }

    @Test
    void 재처리에_성공하면_발주_품목으로_배정하고_해결_상태로_저장한다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(loadOrderForAssignmentPort.load(100L)).willReturn(Optional.of(order(true)));
        given(assignWarehouseUseCase.assign(any())).willReturn(new WarehouseAssignmentResult(100L, 7L));
        givenSaveReturnsArgument();

        // when
        FailedAssignmentResult result = service.retry(1L);

        // then
        ArgumentCaptor<AssignWarehouseCommand> captor = ArgumentCaptor.forClass(AssignWarehouseCommand.class);
        verify(assignWarehouseUseCase).assign(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(100L);
        assertThat(captor.getValue().storeId()).isEqualTo(10L);
        assertThat(captor.getValue().items()).hasSize(1);
        assertThat(captor.getValue().items().get(0).orderItemId()).isEqualTo(5L);
        assertThat(captor.getValue().items().get(0).quantity()).isEqualTo(30);
        assertThat(result.status()).isEqualTo(FailedEventStatus.RESOLVED);
        assertThat(result.retryCount()).isEqualTo(1);
    }

    @Test
    void 배정이_다시_실패하면_예외를_던지지_않고_대기_상태로_시도_횟수와_오류를_기록한다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(loadOrderForAssignmentPort.load(100L)).willReturn(Optional.of(order(true)));
        given(assignWarehouseUseCase.assign(any())).willThrow(new NoAssignableWarehouseException());
        givenSaveReturnsArgument();

        // when
        FailedAssignmentResult result = service.retry(1L);

        // then
        assertThat(result.status()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(result.retryCount()).isEqualTo(1);
        assertThat(result.errorMessage()).isEqualTo(OmsErrorCode.NO_ASSIGNABLE_WAREHOUSE.getMessage());
    }

    @Test
    void 발주가_배정_대기_상태가_아니면_배정을_시도하지_않고_사유를_기록한다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(loadOrderForAssignmentPort.load(100L)).willReturn(Optional.of(order(false)));
        givenSaveReturnsArgument();

        // when
        FailedAssignmentResult result = service.retry(1L);

        // then
        verify(assignWarehouseUseCase, never()).assign(any());
        assertThat(result.status()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(result.errorMessage()).isEqualTo(OmsErrorCode.ORDER_NOT_AWAITING_ASSIGNMENT.getMessage());
    }

    @Test
    void 발주를_찾을_수_없으면_배정을_시도하지_않고_사유를_기록한다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(loadOrderForAssignmentPort.load(100L)).willReturn(Optional.empty());
        givenSaveReturnsArgument();

        // when
        FailedAssignmentResult result = service.retry(1L);

        // then
        verify(assignWarehouseUseCase, never()).assign(any());
        assertThat(result.errorMessage()).isEqualTo(OmsErrorCode.ASSIGNMENT_ORDER_NOT_FOUND.getMessage());
    }

    @Test
    void 이미_처리된_건을_재처리하면_예외를_던지고_배정하지_않는다() {
        // given
        FailedAssignment resolved = pending();
        resolved.markResolvedByRetry(LocalDateTime.now());
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(resolved));

        // when & then
        assertThatThrownBy(() -> service.retry(1L)).isInstanceOf(FailedAssignmentNotPendingException.class);
        verify(assignWarehouseUseCase, never()).assign(any());
        verify(failurePort, never()).save(any());
    }

    @Test
    void 존재하지_않는_건을_재처리하면_FailedAssignmentNotFoundException_예외를_던진다() {
        given(failurePort.findByIdForUpdate(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.retry(999L)).isInstanceOf(FailedAssignmentNotFoundException.class);
    }

    @Test
    void 폐기하면_폐기_상태로_저장하고_배정은_시도하지_않는다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        givenSaveReturnsArgument();

        // when
        FailedAssignmentResult result = service.discard(1L);

        // then
        assertThat(result.status()).isEqualTo(FailedEventStatus.DISCARDED);
        verify(assignWarehouseUseCase, never()).assign(any());
    }

    @Test
    void 이미_처리된_건을_폐기하면_FailedAssignmentNotPendingException_예외를_던진다() {
        // given
        FailedAssignment discarded = pending();
        discarded.discard(LocalDateTime.now());
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(discarded));

        // when & then
        assertThatThrownBy(() -> service.discard(1L)).isInstanceOf(FailedAssignmentNotPendingException.class);
    }

    @Test
    void 재처리에_성공하면_결과를_INFO_로그로_남긴다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(loadOrderForAssignmentPort.load(100L)).willReturn(Optional.of(order(true)));
        given(assignWarehouseUseCase.assign(any())).willReturn(new WarehouseAssignmentResult(100L, 7L));
        givenSaveReturnsArgument();

        try (LogCapture logs = LogCapture.of(FailedAssignmentService.class)) {
            // when
            service.retry(1L);

            // then
            assertThat(logs.messages(Level.INFO)).singleElement().satisfies(message ->
                    assertThat(message).contains("failedEventId=1").contains("orderId=100").contains("RESOLVED"));
        }
    }

    @Test
    void 재처리가_실패하면_사유와_함께_WARN_로그로_남긴다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(loadOrderForAssignmentPort.load(100L)).willReturn(Optional.of(order(true)));
        given(assignWarehouseUseCase.assign(any())).willThrow(new NoAssignableWarehouseException());
        givenSaveReturnsArgument();

        try (LogCapture logs = LogCapture.of(FailedAssignmentService.class)) {
            // when
            service.retry(1L);

            // then
            assertThat(logs.messages(Level.WARN)).singleElement().satisfies(message ->
                    assertThat(message).contains("failedEventId=1").contains("retryCount=1")
                            .contains(OmsErrorCode.NO_ASSIGNABLE_WAREHOUSE.getMessage()));
        }
    }

    @Test
    void 폐기하면_INFO_로그로_남긴다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        givenSaveReturnsArgument();

        try (LogCapture logs = LogCapture.of(FailedAssignmentService.class)) {
            // when
            service.discard(1L);

            // then
            assertThat(logs.messages(Level.INFO)).singleElement().satisfies(message ->
                    assertThat(message).contains("failedEventId=1").contains("DISCARDED"));
        }
    }
}
