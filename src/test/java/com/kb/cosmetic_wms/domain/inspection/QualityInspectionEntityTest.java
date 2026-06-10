package com.kb.cosmetic_wms.domain.inspection;

import com.kb.cosmetic_wms.domain.inspection.constants.QualityConstants;
import com.kb.cosmetic_wms.domain.inspection.entity.QualityInspection;
import com.kb.cosmetic_wms.domain.inspection.enums.InspectionResult;
import com.kb.cosmetic_wms.domain.inspection.fixture.QualityInspectionTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QualityInspectionEntityTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "    "})
    void 품질_검사_결과가_불합격인데_부적합_사유가_누락되면_예외를_던진다(String invalidDefectReason) {
        assertThatThrownBy(() ->
                new QualityInspectionTestBuilder()
                        .result(InspectionResult.FAILED)
                        .defectReason(invalidDefectReason)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(QualityConstants.DEFECT_REASON_REQUIRED_MESSAGE);
    }

    @Test
    void 품질_검사_기록은_합격_결과와_올바른_식별자_ID들로_정상_생성되어야_한다() {
        // given & when
        QualityInspection qualityInspection = new QualityInspectionTestBuilder().build();

        // then
        assertThat(qualityInspection.getResult()).isEqualTo(InspectionResult.PASSED);
        assertThat(qualityInspection.getDefectReason()).isNull();
    }

    @Test
    void 품질_검사_결과가_불합격일_때_부적합_사유가_존재하면_정상_생성되어야_한다() {
        // given & when
        String defectReason = "불합격 사유!";
        QualityInspection qualityInspection = new QualityInspectionTestBuilder()
                .failed(defectReason)
                .build();

        // then
        assertThat(qualityInspection.getDefectReason()).isEqualTo(defectReason);
    }

    @Test
    void 품질_검사_기록_생성_시_입고_품목_식별자_ID가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new QualityInspectionTestBuilder()
                        .inboundItemId(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(QualityConstants.INBOUND_ITEM_ID_REQUIRED_MESSAGE);
    }

    @Test
    void 품질_검사_기록_생성_시_재고_식별자_ID가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new QualityInspectionTestBuilder()
                        .inventoryId(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(QualityConstants.INVENTORY_ID_REQUIRED_MESSAGE);
    }

    @Test
    void 품질_검사_기록_생성_시_검사자_식별자_ID가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new QualityInspectionTestBuilder()
                        .inspectorId(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(QualityConstants.INSPECTOR_ID_REQUIRED_MESSAGE);
    }

    @Test
    void 품질_검사_기록_생성_시_검사_결과가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new QualityInspectionTestBuilder()
                        .result(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(QualityConstants.INSPECTION_RESULT_REQUIRED_MESSAGE);
    }

    @Test
    void 품질_검사_결과가_합격이면_부적합_사유를_입력하더라도_사유가_null로_안전하게_지워져야_한다() {
        // given & when
        QualityInspection qualityInspection = new QualityInspectionTestBuilder()
                .result(InspectionResult.PASSED)
                .defectReason("합격인데 실수로 적은 불량 사유")
                .build();

        // then
        assertThat(qualityInspection.getResult()).isEqualTo(InspectionResult.PASSED);
        assertThat(qualityInspection.getDefectReason()).isNull();
    }
}
