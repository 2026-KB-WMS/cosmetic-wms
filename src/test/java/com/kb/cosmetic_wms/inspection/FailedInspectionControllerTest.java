package com.kb.cosmetic_wms.inspection;

import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import com.kb.cosmetic_wms.inspection.adapter.in.web.FailedInspectionController;
import com.kb.cosmetic_wms.inspection.application.port.in.FailedInspectionResult;
import com.kb.cosmetic_wms.inspection.application.port.in.FindFailedInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.ResolveFailedInspectionUseCase;
import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.exception.FailedInspectionNotFoundException;
import com.kb.cosmetic_wms.inspection.domain.exception.FailedInspectionNotPendingException;
import com.kb.cosmetic_wms.support.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.FAILED_EVENT;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.FAILED_INSPECTION_RESPONSE;
import static com.kb.cosmetic_wms.inspection.fixture.FailedInspectionDocumentUtils.getFailedInspectionResponseFields;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FailedInspectionController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class FailedInspectionControllerTest extends RestDocsSupport {

    @MockitoBean
    private FindFailedInspectionUseCase findFailedInspectionUseCase;

    @MockitoBean
    private ResolveFailedInspectionUseCase resolveFailedInspectionUseCase;

    private FailedInspectionResult result(FailedEventStatus status, int retryCount) {
        return new FailedInspectionResult(1L, 10L, 20L, 30L, 40L, 50, "LOT0001", LocalDate.of(2027, 1, 1),
                "존재하지 않는 로트입니다.", LocalDateTime.of(2026, 11, 3, 12, 0), status, retryCount,
                status == FailedEventStatus.PENDING ? null : LocalDateTime.of(2026, 11, 3, 13, 0));
    }

    @Test
    void 처리_대기_중인_실패_건_목록을_조회한다() throws Exception {
        given(findFailedInspectionUseCase.findByStatus(FailedEventStatus.PENDING))
                .willReturn(List.of(result(FailedEventStatus.PENDING, 0)));

        mockMvc.perform(get("/api/v1/admin/failed-inspections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].failedEventId").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andDo(document("failed-inspection-list",
                        buildParams(FAILED_EVENT, "검사 전표 생성 실패 건 목록 조회 (기본: 처리 대기)")
                ));
    }

    @Test
    void 상태를_지정해_실패_건을_조회한다() throws Exception {
        given(findFailedInspectionUseCase.findByStatus(FailedEventStatus.RESOLVED))
                .willReturn(List.of(result(FailedEventStatus.RESOLVED, 1)));

        mockMvc.perform(get("/api/v1/admin/failed-inspections").param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("RESOLVED"));
    }

    @Test
    void 알_수_없는_상태로_조회하면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/failed-inspections").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 재처리에_성공하면_해결된_실패_건을_반환하고_API_문서가_생성된다() throws Exception {
        given(resolveFailedInspectionUseCase.retry(1L)).willReturn(result(FailedEventStatus.RESOLVED, 1));

        mockMvc.perform(post("/api/v1/admin/failed-inspections/{failedEventId}/retry", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.retryCount").value(1))
                .andDo(document("failed-inspection-retry",
                        buildParams(FAILED_EVENT, "검사 전표 생성 재처리 (실패해도 200이며 status와 errorMessage로 확인)", null, FAILED_INSPECTION_RESPONSE),
                        getFailedInspectionResponseFields()
                ));
    }

    @Test
    void 재처리가_다시_실패해도_200과_대기_상태를_반환한다() throws Exception {
        given(resolveFailedInspectionUseCase.retry(1L)).willReturn(result(FailedEventStatus.PENDING, 2));

        mockMvc.perform(post("/api/v1/admin/failed-inspections/{failedEventId}/retry", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.retryCount").value(2))
                .andExpect(jsonPath("$.errorMessage").value("존재하지 않는 로트입니다."));
    }

    @Test
    void 존재하지_않는_건을_재처리하면_404를_반환한다() throws Exception {
        given(resolveFailedInspectionUseCase.retry(999L)).willThrow(new FailedInspectionNotFoundException());

        mockMvc.perform(post("/api/v1/admin/failed-inspections/{failedEventId}/retry", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("FAILED_INSPECTION_NOT_FOUND"))
                .andDo(document("failed-inspection-retry-fail-not-found",
                        buildErrorParams(FAILED_EVENT, "검사 전표 생성 재처리 실패 - 존재하지 않는 건")
                ));
    }

    @Test
    void 이미_처리된_건을_재처리하면_409를_반환한다() throws Exception {
        given(resolveFailedInspectionUseCase.retry(1L)).willThrow(new FailedInspectionNotPendingException());

        mockMvc.perform(post("/api/v1/admin/failed-inspections/{failedEventId}/retry", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("FAILED_INSPECTION_NOT_PENDING"))
                .andDo(document("failed-inspection-retry-fail-not-pending",
                        buildErrorParams(FAILED_EVENT, "검사 전표 생성 재처리 실패 - 이미 처리된 건")
                ));
    }

    @Test
    void 폐기하면_폐기된_실패_건을_반환하고_API_문서가_생성된다() throws Exception {
        given(resolveFailedInspectionUseCase.discard(1L)).willReturn(result(FailedEventStatus.DISCARDED, 0));

        mockMvc.perform(post("/api/v1/admin/failed-inspections/{failedEventId}/discard", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISCARDED"))
                .andDo(document("failed-inspection-discard",
                        buildParams(FAILED_EVENT, "검사 전표 생성 실패 건 폐기 (더 이상 처리할 필요가 없을 때)", null, FAILED_INSPECTION_RESPONSE)
                ));
    }
}
