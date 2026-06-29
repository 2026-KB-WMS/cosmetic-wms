package com.kb.cosmetic_wms.inspection;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.global.event.InspectionCompletedEvent;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionPort;
import com.kb.cosmetic_wms.inspection.application.service.InspectionService;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionCompleteNotAllowedException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionNotFoundException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionStartNotAllowedException;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inspection.domain.model.Inspection;
import com.kb.cosmetic_wms.inspection.fixture.InspectionTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InspectionServiceTest {

    @InjectMocks
    private InspectionService inspectionService;

    @Mock
    private InspectionPort inspectionPort;

    @Mock
    private EventPublisher eventPublisher;

    private Inspection waitingInspection;
    private Inspection inProgressInspection;
    private Inspection completedInspection;

    @BeforeEach
    void setUp() {
        // sourceId = 10L, productId = 1L, lotId = 100L, warehouseId = 300L, inspectionQuantity = 10
        waitingInspection = new InspectionTestBuilder().buildPending();
        inProgressInspection = new InspectionTestBuilder().buildInProgress();
        completedInspection = new InspectionTestBuilder().buildCompleted();
        ReflectionTestUtils.setField(waitingInspection, "id", 1L);
        ReflectionTestUtils.setField(inProgressInspection, "id", 2L);
        ReflectionTestUtils.setField(completedInspection, "id", 3L);
    }

    @Nested
    class 품질_검사_시작_및_진행 {

        @Test
        void 검사_대기_상태인_품질_검사_전표는_검사_시작_시_검사_진행_중_상태로_정상_전환된다() {
            // given
            when(inspectionPort.findByIdForUpdate(1L)).thenReturn(Optional.of(waitingInspection));
            when(inspectionPort.save(any())).thenReturn(waitingInspection);

            // when
            inspectionService.start(1L, 14L);

            // then
            assertThat(waitingInspection.getStatus()).isEqualTo(InspectionStatus.IN_PROGRESS);
            assertThat(waitingInspection.getInspectorId()).isEqualTo(14L);
        }

        @Test
        void 이미_진행_중이거나_검사_완료된_전표에_대해_다시_검사_시작을_요청하면_예외를_던진다() {
            // given
            when(inspectionPort.findByIdForUpdate(2L)).thenReturn(Optional.of(inProgressInspection));
            when(inspectionPort.findByIdForUpdate(3L)).thenReturn(Optional.of(completedInspection));

            // then
            assertThatThrownBy(() -> inspectionService.start(2L, 14L))
                    .isInstanceOf(InspectionStartNotAllowedException.class);

            assertThatThrownBy(() -> inspectionService.start(3L, 14L))
                    .isInstanceOf(InspectionStartNotAllowedException.class);
        }

        @Test
        void 존재하지_않는_품질_검사_전표_ID로_검사_시작을_요청하면_예외를_던진다() {
            // given
            when(inspectionPort.findByIdForUpdate(999L)).thenReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> inspectionService.start(999L, 14L))
                    .isInstanceOf(InspectionNotFoundException.class);
        }
    }

    @Nested
    class 품질_검사_완료 {

        @Test
        void 완료_처리하면_COMPLETED_상태로_전환된다() {
            // given
            when(inspectionPort.findByIdForUpdate(2L)).thenReturn(Optional.of(inProgressInspection));
            when(inspectionPort.save(any())).thenReturn(inProgressInspection);

            // when
            inspectionService.complete(2L, 10, 0, null);

            // then
            assertThat(inProgressInspection.getStatus()).isEqualTo(InspectionStatus.COMPLETED);
            assertThat(inProgressInspection.getPassedQuantity()).isEqualTo(10);
            assertThat(inProgressInspection.getFailedQuantity()).isZero();
        }

        @Test
        void 합격_반려_수량에_관계없이_완료_처리_시_검사_완료_이벤트가_항상_발행된다() {
            // given
            Inspection inProgressForReject = new InspectionTestBuilder()
                    .inspectionQuantity(10).buildInProgress();
            ReflectionTestUtils.setField(inProgressForReject, "id", 4L);

            when(inspectionPort.findByIdForUpdate(2L)).thenReturn(Optional.of(inProgressInspection));
            when(inspectionPort.findByIdForUpdate(4L)).thenReturn(Optional.of(inProgressForReject));
            when(inspectionPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // when
            inspectionService.complete(2L, 10, 0, null);
            inspectionService.complete(4L, 0, 10, "전량 불량");

            // then
            verify(eventPublisher, times(2)).publish(any(InspectionCompletedEvent.class));
        }

        @Test
        void 완료_이벤트에_검사_결과_수량이_정확히_담긴다() {
            // given
            when(inspectionPort.findByIdForUpdate(2L)).thenReturn(Optional.of(inProgressInspection));
            when(inspectionPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            ArgumentCaptor<InspectionCompletedEvent> captor = ArgumentCaptor.forClass(InspectionCompletedEvent.class);

            // when
            inspectionService.complete(2L, 7, 3, "포장 불량");

            // then
            verify(eventPublisher).publish(captor.capture());
            InspectionCompletedEvent event = captor.getValue();
            assertThat(event.inspectionId()).isEqualTo(2L);
            assertThat(event.sourceId()).isEqualTo(10L);
            assertThat(event.sourceType()).isEqualTo("INBOUND");
            assertThat(event.productId()).isEqualTo(1L);
            assertThat(event.lotId()).isEqualTo(100L);
            assertThat(event.warehouseId()).isEqualTo(300L);
            assertThat(event.passedQuantity()).isEqualTo(7);
            assertThat(event.failedQuantity()).isEqualTo(3);
            assertThat(event.inspectionQuantity()).isEqualTo(10);
            assertThat(event.expiryDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        }

        @Test
        void IN_PROGRESS_상태가_아닌_전표에_완료_처리를_요청하면_예외를_던진다() {
            // given
            when(inspectionPort.findByIdForUpdate(1L)).thenReturn(Optional.of(waitingInspection));
            when(inspectionPort.findByIdForUpdate(3L)).thenReturn(Optional.of(completedInspection));

            // then
            assertThatThrownBy(() -> inspectionService.complete(1L, 10, 0, null))
                    .isInstanceOf(InspectionCompleteNotAllowedException.class);

            assertThatThrownBy(() -> inspectionService.complete(3L, 10, 0, null))
                    .isInstanceOf(InspectionCompleteNotAllowedException.class);
        }

        @Test
        void IN_PROGRESS_상태가_아닌_전표에_완료_처리_시_이벤트는_발행되지_않는다() {
            // given
            when(inspectionPort.findByIdForUpdate(1L)).thenReturn(Optional.of(waitingInspection));

            // when
            assertThatThrownBy(() -> inspectionService.complete(1L, 10, 0, null))
                    .isInstanceOf(InspectionCompleteNotAllowedException.class);

            // then
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        void 존재하지_않는_품질_검사_전표_ID로_완료_처리를_요청하면_예외를_던진다() {
            // given
            when(inspectionPort.findByIdForUpdate(999L)).thenReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> inspectionService.complete(999L, 10, 0, null))
                    .isInstanceOf(InspectionNotFoundException.class);
        }
    }
}
