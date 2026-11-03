package com.kb.cosmetic_wms.oms;

import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import com.kb.cosmetic_wms.oms.adapter.in.web.FailedAssignmentController;
import com.kb.cosmetic_wms.oms.application.port.in.FailedAssignmentResult;
import com.kb.cosmetic_wms.oms.application.port.in.FindFailedAssignmentUseCase;
import com.kb.cosmetic_wms.oms.application.port.in.ResolveFailedAssignmentUseCase;
import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.exception.FailedAssignmentNotFoundException;
import com.kb.cosmetic_wms.oms.domain.exception.FailedAssignmentNotPendingException;
import com.kb.cosmetic_wms.support.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.FAILED_EVENT;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.FAILED_ASSIGNMENT_RESPONSE;
import static com.kb.cosmetic_wms.oms.fixture.FailedAssignmentDocumentUtils.getFailedAssignmentResponseFields;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FailedAssignmentController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class FailedAssignmentControllerTest extends RestDocsSupport {

    @MockitoBean
    private FindFailedAssignmentUseCase findFailedAssignmentUseCase;

    @MockitoBean
    private ResolveFailedAssignmentUseCase resolveFailedAssignmentUseCase;

    private FailedAssignmentResult result(FailedEventStatus status, int retryCount) {
        return new FailedAssignmentResult(1L, 100L, 10L, "발주 품목 전량을 충족할 수 있는 창고가 없습니다.",
                LocalDateTime.of(2026, 11, 3, 12, 0), status, retryCount,
                status == FailedEventStatus.PENDING ? null : LocalDateTime.of(2026, 11, 3, 13, 0));
    }

    @Test
    void 처리_대기_중인_실패_건_목록을_조회한다() throws Exception {
        given(findFailedAssignmentUseCase.findByStatus(FailedEventStatus.PENDING))
                .willReturn(List.of(result(FailedEventStatus.PENDING, 0)));

        mockMvc.perform(get("/api/v1/admin/failed-assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(100))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andDo(document("failed-assignment-list",
                        buildParams(FAILED_EVENT, "창고 배정 실패 건 목록 조회 (기본: 처리 대기)")
                ));
    }

    @Test
    void 재처리에_성공하면_해결된_실패_건을_반환하고_API_문서가_생성된다() throws Exception {
        given(resolveFailedAssignmentUseCase.retry(1L)).willReturn(result(FailedEventStatus.RESOLVED, 1));

        mockMvc.perform(post("/api/v1/admin/failed-assignments/{failedEventId}/retry", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andDo(document("failed-assignment-retry",
                        buildParams(FAILED_EVENT, "창고 배정 재처리 (실패해도 200이며 status와 errorMessage로 확인)", null, FAILED_ASSIGNMENT_RESPONSE),
                        getFailedAssignmentResponseFields()
                ));
    }

    @Test
    void 재처리가_다시_실패해도_200과_대기_상태를_반환한다() throws Exception {
        given(resolveFailedAssignmentUseCase.retry(1L)).willReturn(result(FailedEventStatus.PENDING, 3));

        mockMvc.perform(post("/api/v1/admin/failed-assignments/{failedEventId}/retry", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.retryCount").value(3));
    }

    @Test
    void 존재하지_않는_건을_재처리하면_404를_반환한다() throws Exception {
        given(resolveFailedAssignmentUseCase.retry(999L)).willThrow(new FailedAssignmentNotFoundException());

        mockMvc.perform(post("/api/v1/admin/failed-assignments/{failedEventId}/retry", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("FAILED_ASSIGNMENT_NOT_FOUND"))
                .andDo(document("failed-assignment-retry-fail-not-found",
                        buildErrorParams(FAILED_EVENT, "창고 배정 재처리 실패 - 존재하지 않는 건")
                ));
    }

    @Test
    void 이미_처리된_건을_재처리하면_409를_반환한다() throws Exception {
        given(resolveFailedAssignmentUseCase.retry(1L)).willThrow(new FailedAssignmentNotPendingException());

        mockMvc.perform(post("/api/v1/admin/failed-assignments/{failedEventId}/retry", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("FAILED_ASSIGNMENT_NOT_PENDING"))
                .andDo(document("failed-assignment-retry-fail-not-pending",
                        buildErrorParams(FAILED_EVENT, "창고 배정 재처리 실패 - 이미 처리된 건")
                ));
    }

    @Test
    void 폐기하면_폐기된_실패_건을_반환하고_API_문서가_생성된다() throws Exception {
        given(resolveFailedAssignmentUseCase.discard(1L)).willReturn(result(FailedEventStatus.DISCARDED, 0));

        mockMvc.perform(post("/api/v1/admin/failed-assignments/{failedEventId}/discard", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISCARDED"))
                .andDo(document("failed-assignment-discard",
                        buildParams(FAILED_EVENT, "창고 배정 실패 건 폐기 (발주가 취소되는 등 더 이상 배정할 필요가 없을 때)", null, FAILED_ASSIGNMENT_RESPONSE)
                ));
    }
}
