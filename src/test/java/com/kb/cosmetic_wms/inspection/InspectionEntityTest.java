package com.kb.cosmetic_wms.inspection;

import com.kb.cosmetic_wms.inspection.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionCompleteNotAllowedException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionDefectReasonRequiredException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionErrorCode;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionInspectorIdRequiredException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionNegativeQuantityException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionQuantityInvalidException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionQuantityMismatchException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionSourceIdRequiredException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionSourceTypeRequiredException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionStartNotAllowedException;
import com.kb.cosmetic_wms.inspection.domain.model.Inspection;
import com.kb.cosmetic_wms.inspection.fixture.InspectionTestBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InspectionEntityTest {

    @Nested
    class 최초_전표_생성_단계 {

        @Test
        void 올바른_출처_정보와_수량이_주어지면_WAITING_상태의_전표가_정상적으로_생성된다() {
            Inspection inspection = new InspectionTestBuilder().buildPending();

            assertThat(inspection.getStatus()).isEqualTo(InspectionStatus.WAITING);
            assertThat(inspection.getPassedQuantity()).isZero();
            assertThat(inspection.getFailedQuantity()).isZero();
            assertThat(inspection.getInspectorId()).isNull();
        }

        @Test
        void 출처_타입이_누락되면_전표_생성_시_예외를_던진다() {
            assertThatThrownBy(() ->
                    new InspectionTestBuilder()
                            .sourceType(null)
                            .buildPending()
            )
                    .isInstanceOf(InspectionSourceTypeRequiredException.class)
                    .hasMessage(InspectionErrorCode.INSPECTION_SOURCE_TYPE_REQUIRED.getMessage());
        }

        @Test
        void 출처_대상이_누락되면_전표_생성_시_예외를_던진다() {
            assertThatThrownBy(() ->
                    new InspectionTestBuilder()
                            .sourceId(null)
                            .buildPending()
            )
                    .isInstanceOf(InspectionSourceIdRequiredException.class)
                    .hasMessage(InspectionErrorCode.INSPECTION_SOURCE_ID_REQUIRED.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        void 검사_대상_수량이_0_이하_또는_음수이면_전표_생성에_실패한다(int invalidQuantity) {
            assertThatThrownBy(() ->
                    new InspectionTestBuilder()
                            .inspectionQuantity(invalidQuantity)
                            .buildPending()
            )
                    .isInstanceOf(InspectionQuantityInvalidException.class)
                    .hasMessage(InspectionErrorCode.INSPECTION_QUANTITY_INVALID.getMessage());
        }
    }

    @Nested
    class 품질_검사_시작_단계 {

        @Test
        void WAITING_상태인_전표에_검사관_ID를_지정하면_IN_PROGRESS_상태로_정상_전환된다() {
            Inspection inspection = new InspectionTestBuilder().buildInProgress();

            assertThat(inspection.getStatus()).isEqualTo(InspectionStatus.IN_PROGRESS);
            assertThat(inspection.getInspectorId()).isEqualTo(1L);
        }

        @Test
        void 검사관_ID가_누락된_상태로_검사_시작_요청_시_예외를_던진다() {
            Inspection inspection = new InspectionTestBuilder().buildPending();

            assertThatThrownBy(() -> inspection.startInspection(null))
                    .isInstanceOf(InspectionInspectorIdRequiredException.class)
                    .hasMessage(InspectionErrorCode.INSPECTION_INSPECTOR_ID_REQUIRED.getMessage());
        }

        @Test
        void 이미_검사_진행_상태이거나_완료된_전표에_다시_검사_요청_시_예외를_던진다() {
            Inspection inProgress = new InspectionTestBuilder().buildInProgress();
            Inspection completed = new InspectionTestBuilder().buildCompleted();

            assertThatThrownBy(() -> inProgress.startInspection(1L))
                    .isInstanceOf(InspectionStartNotAllowedException.class)
                    .hasMessage(InspectionErrorCode.INSPECTION_START_NOT_ALLOWED.getMessage());

            assertThatThrownBy(() -> completed.startInspection(1L))
                    .isInstanceOf(InspectionStartNotAllowedException.class)
                    .hasMessage(InspectionErrorCode.INSPECTION_START_NOT_ALLOWED.getMessage());
        }
    }

    @Nested
    class 최종_품질_판정_완료_단계 {

        @Test
        void 합격_수량과_반려_수량의_합이_총_검사_수량과_일치하면_COMPLETED_상태로_완료된다() {
            Inspection inspection = new InspectionTestBuilder()
                    .inspectionQuantity(10)
                    .passedQuantity(7)
                    .failedQuantity(3)
                    .defectReason("외관 불량")
                    .buildCompleted();

            assertThat(inspection.getStatus()).isEqualTo(InspectionStatus.COMPLETED);
            assertThat(inspection.getPassedQuantity()).isEqualTo(7);
            assertThat(inspection.getFailedQuantity()).isEqualTo(3);
        }

        @Test
        void 전량_합격인_경우_반려_사유는_자동으로_null_처리된다() {
            Inspection inspection = new InspectionTestBuilder()
                    .inspectionQuantity(10)
                    .passedQuantity(10)
                    .failedQuantity(0)
                    .defectReason("불필요한 사유 입력")
                    .buildCompleted();

            assertThat(inspection.getDefectReason()).isNull();
        }

        @Test
        void 반려_수량이_1개_이상인_경우_입력된_불합격_사유가_정상_저장된다() {
            String defectReason = "성분 기준 초과";
            Inspection inspection = new InspectionTestBuilder()
                    .inspectionQuantity(10)
                    .passedQuantity(8)
                    .failedQuantity(2)
                    .defectReason(defectReason)
                    .buildCompleted();

            assertThat(inspection.getDefectReason()).isEqualTo(defectReason);
        }

        @Test
        void 합격_수량이나_반려_수량_중_하나라도_음수가_입력되면_예외를_던진다() {
            Inspection inspection = new InspectionTestBuilder().buildInProgress();

            assertThatThrownBy(() -> inspection.completeInspection(-1, 11, null, 200L))
                    .isInstanceOf(InspectionNegativeQuantityException.class)
                    .hasMessage(InspectionErrorCode.INSPECTION_NEGATIVE_QUANTITY.getMessage());
        }

        @Test
        void 합격_수량과_반려_수량의_합이_총_검사_수량과_일치하지_않으면_예외를_던진다() {
            Inspection inspection = new InspectionTestBuilder()
                    .inspectionQuantity(10)
                    .buildInProgress();

            assertThatThrownBy(() -> inspection.completeInspection(5, 3, null, 200L))
                    .isInstanceOf(InspectionQuantityMismatchException.class)
                    .hasMessage("합격 수량과 반려 수량의 합이 총 검사 수량(10)과 일치해야 합니다.");
        }

        @Test
        void 반려_수량이_존재함에도_불합격_사유가_null이거나_공백이면_예외를_던진다() {
            Inspection inspectionForNull = new InspectionTestBuilder()
                    .inspectionQuantity(10)
                    .buildInProgress();
            Inspection inspectionForBlank = new InspectionTestBuilder()
                    .inspectionQuantity(10)
                    .buildInProgress();

            assertThatThrownBy(() -> inspectionForNull.completeInspection(8, 2, null, 200L))
                    .isInstanceOf(InspectionDefectReasonRequiredException.class)
                    .hasMessage(InspectionErrorCode.INSPECTION_DEFECT_REASON_REQUIRED.getMessage());

            assertThatThrownBy(() -> inspectionForBlank.completeInspection(8, 2, "   ", 200L))
                    .isInstanceOf(InspectionDefectReasonRequiredException.class)
                    .hasMessage(InspectionErrorCode.INSPECTION_DEFECT_REASON_REQUIRED.getMessage());
        }

        @Test
        void IN_PROGRESS_상태가_아닌_전표에_완료_처리_요청_시_예외를_던진다() {
            Inspection waiting = new InspectionTestBuilder().buildPending();

            assertThatThrownBy(() -> waiting.completeInspection(10, 0, null, 200L))
                    .isInstanceOf(InspectionCompleteNotAllowedException.class)
                    .hasMessage(InspectionErrorCode.INSPECTION_COMPLETE_NOT_ALLOWED.getMessage());
        }
    }
}