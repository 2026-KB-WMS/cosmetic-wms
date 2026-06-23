package com.kb.cosmetic_wms.domain.inspection;

import com.kb.cosmetic_wms.domain.inspection.entity.QualityInspection;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionStatus;
import com.kb.cosmetic_wms.global.event.InspectionCompletedEvent;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionCompleteNotAllowedException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionNotFoundException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionSourceIdRequiredException;
import com.kb.cosmetic_wms.domain.inspection.exception.InspectionStartNotAllowedException;
import com.kb.cosmetic_wms.domain.inspection.fixture.QualityInspectionTestBuilder;
import com.kb.cosmetic_wms.domain.inspection.repository.InspectionRepository;
import com.kb.cosmetic_wms.domain.inspection.service.InspectionService;
import com.kb.cosmetic_wms.global.event.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    private InspectionRepository inspectionRepository;

    @Mock
    private EventPublisher eventPublisher;

    private QualityInspection waitingQualityInspection;
    private QualityInspection inProgressQualityInspection;
    private QualityInspection completedQualityInspection;

    @BeforeEach
    void setUp() {
        // sourceId = 10L, inventoryId = 100L, inspectionQuantity = 10
        waitingQualityInspection = new QualityInspectionTestBuilder().buildPending();
        inProgressQualityInspection = new QualityInspectionTestBuilder().buildInProgress();
        completedQualityInspection = new QualityInspectionTestBuilder().buildCompleted();
        ReflectionTestUtils.setField(waitingQualityInspection, "id", 1L);
        ReflectionTestUtils.setField(inProgressQualityInspection, "id", 2L);
        ReflectionTestUtils.setField(completedQualityInspection, "id", 3L);
    }

    @Nested
    class 품질_검사_전표_생성 {

        @Test
        void 품질_검사_의뢰_이벤트가_들어오면_출처와_수량을_기반으로_품질_검사_전표가_대기_상태로_생성된다() {
            // given
            when(inspectionRepository.save(any(QualityInspection.class)))
                    .thenReturn(waitingQualityInspection);

            // when
            QualityInspection result = inspectionService.createInboundInspection(
                    10L, 1L, 100L, 200L, 300L, 10);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(InspectionStatus.WAITING);
            assertThat(result.getSourceId()).isEqualTo(10L);
            assertThat(result.getInspectorId()).isNull();
        }

        @Test
        void 유효하지_않은_입고_전표인_경우_품질_검사_전표_생성에_실패한다() {
            assertThatThrownBy(() ->
                    inspectionService.createInboundInspection(null, 1L, 100L, 200L, 300L, 10))
                    .isInstanceOf(InspectionSourceIdRequiredException.class);
        }
    }

    @Nested
    class 품질_검사_시작_및_진행 {

        @Test
        void 검사_대기_상태인_품질_검사_전표는_검사_시작_시_검사_진행_중_상태로_정상_전환된다() {
            // given
            when(inspectionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(waitingQualityInspection));

            // when
            inspectionService.startInspection(1L, 14L);

            // then
            assertThat(waitingQualityInspection.getStatus()).isEqualTo(InspectionStatus.IN_PROGRESS);
            assertThat(waitingQualityInspection.getInspectorId()).isEqualTo(14L);
        }

        @Test
        void 이미_진행_중이거나_검사_완료된_전표에_대해_다시_검사_시작을_요청하면_예외를_던진다() {
            // given
            when(inspectionRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(inProgressQualityInspection));
            when(inspectionRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(completedQualityInspection));

            // then
            assertThatThrownBy(() -> inspectionService.startInspection(2L, 14L))
                    .isInstanceOf(InspectionStartNotAllowedException.class);

            assertThatThrownBy(() -> inspectionService.startInspection(3L, 14L))
                    .isInstanceOf(InspectionStartNotAllowedException.class);
        }

        @Test
        void 존재하지_않는_품질_검사_전표_ID로_검사_시작을_요청하면_예외를_던진다() {
            // given
            when(inspectionRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> inspectionService.startInspection(999L, 14L))
                    .isInstanceOf(InspectionNotFoundException.class);
        }
    }

    @Nested
    class 품질_검사_완료 {

        @Test
        void 완료_처리하면_COMPLETED_상태로_전환된다() {
            // given
            when(inspectionRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(inProgressQualityInspection));

            // when
            inspectionService.completeInspection(2L, 10, 0, null);

            // then
            assertThat(inProgressQualityInspection.getStatus()).isEqualTo(InspectionStatus.COMPLETED);
            assertThat(inProgressQualityInspection.getPassedQuantity()).isEqualTo(10);
            assertThat(inProgressQualityInspection.getFailedQuantity()).isZero();
        }

        @Test
        void 합격_반려_수량에_관계없이_완료_처리_시_검사_완료_이벤트가_항상_발행된다() {
            // given
            when(inspectionRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(inProgressQualityInspection));
            QualityInspection inProgressForReject = new QualityInspectionTestBuilder()
                    .inspectionQuantity(10).buildInProgress();
            ReflectionTestUtils.setField(inProgressForReject, "id", 4L);
            when(inspectionRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(inProgressForReject));

            // when
            inspectionService.completeInspection(2L, 10, 0, null);
            inspectionService.completeInspection(4L, 0, 10, "전량 불량");

            // then
            verify(eventPublisher, times(2)).publish(any(InspectionCompletedEvent.class));
        }

        @Test
        void 완료_이벤트에_검사_결과_수량이_정확히_담긴다() {
            // given
            when(inspectionRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(inProgressQualityInspection));
            ArgumentCaptor<InspectionCompletedEvent> captor = ArgumentCaptor.forClass(InspectionCompletedEvent.class);

            // when
            inspectionService.completeInspection(2L, 7, 3, "포장 불량");

            // then
            verify(eventPublisher).publish(captor.capture());
            InspectionCompletedEvent event = captor.getValue();
            assertThat(event.inspectionId()).isEqualTo(2L);
            assertThat(event.sourceId()).isEqualTo(10L);
            assertThat(event.sourceType()).isEqualTo("INBOUND");
            assertThat(event.productId()).isEqualTo(1L);
            assertThat(event.lotId()).isEqualTo(100L);
            assertThat(event.sectionId()).isEqualTo(200L);
            assertThat(event.warehouseId()).isEqualTo(300L);
            assertThat(event.passedQuantity()).isEqualTo(7);
            assertThat(event.failedQuantity()).isEqualTo(3);
            assertThat(event.inspectionQuantity()).isEqualTo(10);
        }

        @Test
        void IN_PROGRESS_상태가_아닌_전표에_완료_처리를_요청하면_예외를_던진다() {
            // given
            when(inspectionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(waitingQualityInspection));
            when(inspectionRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(completedQualityInspection));

            // then
            assertThatThrownBy(() -> inspectionService.completeInspection(1L, 10, 0, null))
                    .isInstanceOf(InspectionCompleteNotAllowedException.class);

            assertThatThrownBy(() -> inspectionService.completeInspection(3L, 10, 0, null))
                    .isInstanceOf(InspectionCompleteNotAllowedException.class);
        }

        @Test
        void IN_PROGRESS_상태가_아닌_전표에_완료_처리_시_이벤트는_발행되지_않는다() {
            // given
            when(inspectionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(waitingQualityInspection));

            // when
            assertThatThrownBy(() -> inspectionService.completeInspection(1L, 10, 0, null))
                    .isInstanceOf(InspectionCompleteNotAllowedException.class);

            // then
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        void 존재하지_않는_품질_검사_전표_ID로_완료_처리를_요청하면_예외를_던진다() {
            // given
            when(inspectionRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> inspectionService.completeInspection(999L, 10, 0, null))
                    .isInstanceOf(InspectionNotFoundException.class);
        }
    }
}
