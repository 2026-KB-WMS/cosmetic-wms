package com.kb.cosmetic_wms.oms.fixture;

import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.key;

public final class FailedAssignmentDocumentUtils {

    private FailedAssignmentDocumentUtils() {
    }

    public static ResponseFieldsSnippet getFailedAssignmentResponseFields() {
        return responseFields(
                fieldWithPath("failedEventId").description("실패 건 ID")
                        .attributes(key("example").value(1)),
                fieldWithPath("orderId").description("창고 배정에 실패한 발주 ID")
                        .attributes(key("example").value(100)),
                fieldWithPath("storeId").description("가맹점 ID")
                        .attributes(key("example").value(10)),
                fieldWithPath("errorMessage").description("가장 최근 실패 사유").optional()
                        .attributes(key("example").value("발주 품목 전량을 충족할 수 있는 창고가 없습니다.")),
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
