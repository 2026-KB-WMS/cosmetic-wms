package com.kb.cosmetic_wms.inspection.fixture;

import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.key;

public final class FailedInspectionDocumentUtils {

    private FailedInspectionDocumentUtils() {
    }

    public static ResponseFieldsSnippet getFailedInspectionResponseFields() {
        return responseFields(
                fieldWithPath("failedEventId").description("실패 건 ID")
                        .attributes(key("example").value(1)),
                fieldWithPath("inboundId").description("입고 ID")
                        .attributes(key("example").value(10)),
                fieldWithPath("lineId").description("입고 라인 ID (검사 전표의 출처 ID)")
                        .attributes(key("example").value(20)),
                fieldWithPath("productId").description("상품 ID")
                        .attributes(key("example").value(30)),
                fieldWithPath("warehouseId").description("창고 ID")
                        .attributes(key("example").value(40)),
                fieldWithPath("receivedQuantity").description("검사 대상 수량")
                        .attributes(key("example").value(50)),
                fieldWithPath("manufacturerLotNumber").description("제조사 로트 번호")
                        .attributes(key("example").value("LOT0001")),
                fieldWithPath("expirationDate").description("유통기한").optional()
                        .attributes(key("example").value("2027-01-01")),
                fieldWithPath("errorMessage").description("가장 최근 실패 사유").optional()
                        .attributes(key("example").value("존재하지 않는 로트입니다.")),
                fieldWithPath("failedAt").description("최초 실패 시각")
                        .attributes(key("example").value("2026-11-03T12:00:00")),
                fieldWithPath("status").description("처리 상태 (PENDING: 처리 대기, RESOLVED: 해결, DISCARDED: 폐기)")
                        .attributes(key("example").value("PENDING")),
                fieldWithPath("retryCount").description("수동 재처리 시도 횟수")
                        .attributes(key("example").value(0)),
                fieldWithPath("resolvedAt").description("해결 또는 폐기 시각").optional()
                        .attributes(key("example").value("2026-11-03T13:00:00"))
        );
    }
}
