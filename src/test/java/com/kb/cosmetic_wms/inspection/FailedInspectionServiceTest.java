package com.kb.cosmetic_wms.inspection;

import com.kb.cosmetic_wms.global.transaction.NewTransactionRunner;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionCommand;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.FailedInspectionResult;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionCreationFailurePort;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionPort;
import com.kb.cosmetic_wms.inspection.application.service.FailedInspectionService;
import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.exception.FailedInspectionNotFoundException;
import com.kb.cosmetic_wms.inspection.domain.exception.FailedInspectionNotPendingException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionLotNotFoundException;
import com.kb.cosmetic_wms.inspection.domain.model.FailedInspection;
import com.kb.cosmetic_wms.inspection.domain.model.Inspection;
import com.kb.cosmetic_wms.support.LogCapture;
import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FailedInspectionServiceTest {

    @InjectMocks
    private FailedInspectionService service;

    @Mock
    private InspectionCreationFailurePort failurePort;

    @Mock
    private InspectionPort inspectionPort;

    @Mock
    private CreateInspectionUseCase createInspectionUseCase;

    @Spy
    private NewTransactionRunner newTransactionRunner = new NewTransactionRunner();

    private FailedInspection pending() {
        return FailedInspection.reconstitute(1L, 10L, 20L, 30L, 40L, 50, "LOT0001", LocalDate.of(2027, 1, 1),
                "최초 실패", LocalDateTime.of(2026, 11, 3, 12, 0), FailedEventStatus.PENDING, 0, null);
    }

    private void givenSaveReturnsArgument() {
        given(failurePort.save(any(FailedInspection.class))).willAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void 상태별로_실패_건을_조회한다() {
        given(failurePort.findByStatus(FailedEventStatus.PENDING)).willReturn(List.of(pending()));

        List<FailedInspectionResult> results = service.findByStatus(FailedEventStatus.PENDING);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).lineId()).isEqualTo(20L);
        assertThat(results.get(0).status()).isEqualTo(FailedEventStatus.PENDING);
    }

    @Test
    void 재처리에_성공하면_기록된_값으로_전표를_만들고_해결_상태로_저장한다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(inspectionPort.findBySourceTypeAndSourceId(InspectionSourceType.INBOUND, 20L)).willReturn(Optional.empty());
        givenSaveReturnsArgument();

        // when
        FailedInspectionResult result = service.retry(1L);

        // then
        ArgumentCaptor<CreateInspectionCommand> captor = ArgumentCaptor.forClass(CreateInspectionCommand.class);
        verify(createInspectionUseCase).create(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new CreateInspectionCommand(
                InspectionSourceType.INBOUND, 20L, 50, 30L, 10L, "LOT0001", 40L, LocalDate.of(2027, 1, 1)));
        assertThat(result.status()).isEqualTo(FailedEventStatus.RESOLVED);
        assertThat(result.retryCount()).isEqualTo(1);
        assertThat(result.resolvedAt()).isNotNull();
    }

    @Test
    void 재처리가_다시_실패하면_예외를_던지지_않고_대기_상태로_시도_횟수와_오류를_기록한다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(inspectionPort.findBySourceTypeAndSourceId(InspectionSourceType.INBOUND, 20L)).willReturn(Optional.empty());
        doThrow(new InspectionLotNotFoundException()).when(createInspectionUseCase).create(any());
        givenSaveReturnsArgument();

        // when
        FailedInspectionResult result = service.retry(1L);

        // then
        assertThat(result.status()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(result.retryCount()).isEqualTo(1);
        assertThat(result.errorMessage()).isEqualTo(new InspectionLotNotFoundException().getMessage());
        assertThat(result.resolvedAt()).isNull();
    }

    @Test
    void 메시지가_없는_예외로_실패하면_예외_이름을_오류로_기록한다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(inspectionPort.findBySourceTypeAndSourceId(InspectionSourceType.INBOUND, 20L)).willReturn(Optional.empty());
        doThrow(new IllegalStateException()).when(createInspectionUseCase).create(any());
        givenSaveReturnsArgument();

        // when
        FailedInspectionResult result = service.retry(1L);

        // then
        assertThat(result.errorMessage()).isEqualTo("IllegalStateException");
    }

    @Test
    void 이미_전표가_있으면_중복_생성하지_않고_해결_처리한다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(inspectionPort.findBySourceTypeAndSourceId(InspectionSourceType.INBOUND, 20L))
                .willReturn(Optional.of(mock(Inspection.class)));
        givenSaveReturnsArgument();

        // when
        FailedInspectionResult result = service.retry(1L);

        // then
        verify(createInspectionUseCase, never()).create(any());
        assertThat(result.status()).isEqualTo(FailedEventStatus.RESOLVED);
    }

    @Test
    void 이미_해결된_건을_재처리하면_예외를_던지고_전표를_만들지_않는다() {
        // given
        FailedInspection resolved = pending();
        resolved.markResolvedByRetry(LocalDateTime.now());
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(resolved));

        // when & then
        assertThatThrownBy(() -> service.retry(1L)).isInstanceOf(FailedInspectionNotPendingException.class);
        verify(createInspectionUseCase, never()).create(any());
        verify(failurePort, never()).save(any());
    }

    @Test
    void 존재하지_않는_건을_재처리하면_FailedInspectionNotFoundException_예외를_던진다() {
        given(failurePort.findByIdForUpdate(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.retry(999L)).isInstanceOf(FailedInspectionNotFoundException.class);
    }

    @Test
    void 폐기하면_폐기_상태로_저장한다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        givenSaveReturnsArgument();

        // when
        FailedInspectionResult result = service.discard(1L);

        // then
        assertThat(result.status()).isEqualTo(FailedEventStatus.DISCARDED);
        assertThat(result.resolvedAt()).isNotNull();
        verify(createInspectionUseCase, never()).create(any());
    }

    @Test
    void 이미_처리된_건을_폐기하면_FailedInspectionNotPendingException_예외를_던진다() {
        // given
        FailedInspection discarded = pending();
        discarded.discard(LocalDateTime.now());
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(discarded));

        // when & then
        assertThatThrownBy(() -> service.discard(1L)).isInstanceOf(FailedInspectionNotPendingException.class);
    }

    @Test
    void 재처리에_성공하면_결과를_INFO_로그로_남긴다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(inspectionPort.findBySourceTypeAndSourceId(InspectionSourceType.INBOUND, 20L)).willReturn(Optional.empty());
        givenSaveReturnsArgument();

        try (LogCapture logs = LogCapture.of(FailedInspectionService.class)) {
            // when
            service.retry(1L);

            // then
            assertThat(logs.messages(Level.INFO)).singleElement().satisfies(message ->
                    assertThat(message).contains("failedEventId=1").contains("lineId=20").contains("RESOLVED"));
        }
    }

    @Test
    void 재처리가_실패하면_사유와_함께_WARN_로그로_남긴다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        given(inspectionPort.findBySourceTypeAndSourceId(InspectionSourceType.INBOUND, 20L)).willReturn(Optional.empty());
        doThrow(new InspectionLotNotFoundException()).when(createInspectionUseCase).create(any());
        givenSaveReturnsArgument();

        try (LogCapture logs = LogCapture.of(FailedInspectionService.class)) {
            // when
            service.retry(1L);

            // then
            assertThat(logs.messages(Level.WARN)).singleElement().satisfies(message ->
                    assertThat(message).contains("failedEventId=1").contains("retryCount=1")
                            .contains(new InspectionLotNotFoundException().getMessage()));
        }
    }

    @Test
    void 폐기하면_INFO_로그로_남긴다() {
        // given
        given(failurePort.findByIdForUpdate(1L)).willReturn(Optional.of(pending()));
        givenSaveReturnsArgument();

        try (LogCapture logs = LogCapture.of(FailedInspectionService.class)) {
            // when
            service.discard(1L);

            // then
            assertThat(logs.messages(Level.INFO)).singleElement().satisfies(message ->
                    assertThat(message).contains("failedEventId=1").contains("DISCARDED"));
        }
    }
}
