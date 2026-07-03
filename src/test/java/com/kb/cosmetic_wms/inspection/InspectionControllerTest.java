package com.kb.cosmetic_wms.inspection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import com.kb.cosmetic_wms.inspection.adapter.in.web.InspectionCompleteRequest;
import com.kb.cosmetic_wms.inspection.adapter.in.web.InspectionStartRequest;
import com.kb.cosmetic_wms.inspection.adapter.in.web.InspectionController;
import com.kb.cosmetic_wms.inspection.application.port.in.FindInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.InspectionLifecycleUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.InspectionResult;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionCompleteNotAllowedException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionNotFoundException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionQuantityMismatchException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionStartNotAllowedException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.INSPECTION;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {InspectionController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class InspectionControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FindInspectionUseCase findInspectionUseCase;

    @MockitoBean
    private InspectionLifecycleUseCase inspectionLifecycleUseCase;

    @Nested
    class 품질_검사_전표_조회 {

        @Test
        @WithMockUser
        void 올바른_전표_ID로_조회하면_200_OK와_상세정보를_반환한다() throws Exception {
            // given
            Long inspectionId = 1L;
            InspectionResult result = new InspectionResult(
                    inspectionId, InspectionSourceType.INBOUND, 10L,
                    5L, null, InspectionStatus.WAITING, 20, 0, 0, null);

            given(findInspectionUseCase.findById(inspectionId)).willReturn(result);

            // when & then
            mockMvc.perform(get("/api/v1/quality-inspections/{inspectionId}", inspectionId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(inspectionId))
                    .andExpect(jsonPath("$.lotId").value(5L))
                    .andExpect(jsonPath("$.status").value("WAITING"))
                    .andExpect(jsonPath("$.inspectionQuantity").value(20))
                    .andDo(document("inspection-get-success",
                            buildParams(INSPECTION, "품질 검사 전표 단건 조회", null, INSPECTION_DETAIL_RESPONSE),
                            createResponseFields(getInspectionDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_전표_ID로_조회하면_404_NOT_FOUND를_반환한다() throws Exception {
            // given
            Long invalidId = 999L;
            given(findInspectionUseCase.findById(invalidId)).willThrow(new InspectionNotFoundException());

            // when & then
            mockMvc.perform(get("/api/v1/quality-inspections/{inspectionId}", invalidId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INSPECTION_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(InspectionErrorCode.INSPECTION_NOT_FOUND.getMessage()))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andDo(document("inspection-get-fail-not-found",
                            buildErrorParams(INSPECTION, "품질 검사 전표 단건 조회"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 품질_검사_시작 {

        @Test
        @WithMockUser
        void 올바른_검사자_ID로_시작_요청하면_200_OK와_갱신된_전표를_반환한다() throws Exception {
            // given
            Long inspectionId = 1L;
            InspectionStartRequest request = new InspectionStartRequest(14L);
            InspectionResult result = new InspectionResult(
                    inspectionId, InspectionSourceType.INBOUND, 10L,
                    5L, 14L, InspectionStatus.IN_PROGRESS, 20, 0, 0, null);

            given(inspectionLifecycleUseCase.start(eq(inspectionId), eq(14L))).willReturn(result);

            // when & then
            mockMvc.perform(patch("/api/v1/quality-inspections/{inspectionId}/start", inspectionId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.inspectorId").value(14L))
                    .andDo(document("inspection-start-success",
                            buildParams(INSPECTION, "품질 검사 시작", INSPECTION_START_REQUEST, INSPECTION_DETAIL_RESPONSE),
                            createRequestFields(getInspectionStartRequestFields()),
                            createResponseFields(getInspectionDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 검사자_ID가_누락되면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            InspectionStartRequest request = new InspectionStartRequest(null);

            // when & then
            mockMvc.perform(patch("/api/v1/quality-inspections/{inspectionId}/start", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("inspection-start-fail-no-inspector",
                            buildErrorParams(INSPECTION, "품질 검사 시작"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void WAITING이_아닌_상태에서_시작_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            InspectionStartRequest request = new InspectionStartRequest(14L);
            given(inspectionLifecycleUseCase.start(anyLong(), anyLong()))
                    .willThrow(new InspectionStartNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/quality-inspections/{inspectionId}/start", 2L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("INSPECTION_START_NOT_ALLOWED"))
                    .andDo(document("inspection-start-fail-not-allowed",
                            buildErrorParams(INSPECTION, "품질 검사 시작"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 품질_검사_완료 {

        @Test
        @WithMockUser
        void 올바른_수량으로_완료_요청하면_200_OK와_완료된_전표를_반환한다() throws Exception {
            // given
            Long inspectionId = 2L;
            InspectionCompleteRequest request = new InspectionCompleteRequest(18, 2, "포장 불량");
            InspectionResult result = new InspectionResult(
                    inspectionId, InspectionSourceType.INBOUND, 10L,
                    5L, 14L, InspectionStatus.COMPLETED, 20, 18, 2, "포장 불량");

            given(inspectionLifecycleUseCase.complete(eq(inspectionId), eq(18), eq(2), eq("포장 불량")))
                    .willReturn(result);

            // when & then
            mockMvc.perform(patch("/api/v1/quality-inspections/{inspectionId}/complete", inspectionId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.passedQuantity").value(18))
                    .andExpect(jsonPath("$.failedQuantity").value(2))
                    .andExpect(jsonPath("$.defectReason").value("포장 불량"))
                    .andDo(document("inspection-complete-success",
                            buildParams(INSPECTION, "품질 검사 완료", INSPECTION_COMPLETE_REQUEST, INSPECTION_DETAIL_RESPONSE),
                            createRequestFields(getInspectionCompleteRequestFields()),
                            createResponseFields(getInspectionDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void IN_PROGRESS가_아닌_상태에서_완료_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            InspectionCompleteRequest request = new InspectionCompleteRequest(20, 0, null);
            given(inspectionLifecycleUseCase.complete(anyLong(), anyInt(), anyInt(), any()))
                    .willThrow(new InspectionCompleteNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/quality-inspections/{inspectionId}/complete", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("INSPECTION_COMPLETE_NOT_ALLOWED"))
                    .andDo(document("inspection-complete-fail-not-allowed",
                            buildErrorParams(INSPECTION, "품질 검사 완료"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 수량_합계가_총_검사수량과_불일치하면_422_UNPROCESSABLE_ENTITY를_반환한다() throws Exception {
            // given
            InspectionCompleteRequest request = new InspectionCompleteRequest(5, 5, null);
            given(inspectionLifecycleUseCase.complete(anyLong(), anyInt(), anyInt(), any()))
                    .willThrow(new InspectionQuantityMismatchException(20));

            // when & then
            mockMvc.perform(patch("/api/v1/quality-inspections/{inspectionId}/complete", 2L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errorCode").value("INSPECTION_QUANTITY_MISMATCH"))
                    .andDo(document("inspection-complete-fail-quantity-mismatch",
                            buildErrorParams(INSPECTION, "품질 검사 완료"),
                            globalErrorResponseFields()
                    ));
        }
    }

    private static FieldDescriptor[] getInspectionStartRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("inspectorId").description("검사를 수행할 검사자 ID (필수)")
        };
    }

    private static FieldDescriptor[] getInspectionCompleteRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("passedQuantity").description("합격 판정 수량 (0 이상)"),
                fieldWithPath("failedQuantity").description("반려 판정 수량 (0 이상)"),
                fieldWithPath("defectReason").description("부적합 사유 (반려 수량 > 0인 경우 필수, 그 외 생략 가능)").optional()
        };
    }

    private static FieldDescriptor[] getInspectionDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("품질 검사 전표 ID"),
                fieldWithPath("sourceType").description("검사 요청 출처 유형 (INBOUND 등)"),
                fieldWithPath("sourceId").description("출처 대상 ID (예: 입고 품목 ID)"),
                fieldWithPath("lotId").description("검사 대상 로트 ID"),
                fieldWithPath("inspectorId").description("검사자 ID (검사 시작 전 null)").optional(),
                fieldWithPath("status").description("검사 상태 (WAITING / IN_PROGRESS / COMPLETED)"),
                fieldWithPath("inspectionQuantity").description("총 검사 대상 수량"),
                fieldWithPath("passedQuantity").description("합격 수량"),
                fieldWithPath("failedQuantity").description("반려 수량"),
                fieldWithPath("defectReason").description("부적합 사유 (반려 없는 경우 null)").optional()
        };
    }
}
