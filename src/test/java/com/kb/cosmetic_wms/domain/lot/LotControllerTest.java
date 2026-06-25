package com.kb.cosmetic_wms.domain.lot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import com.kb.cosmetic_wms.lot.adapter.in.web.LotController;
import com.kb.cosmetic_wms.lot.adapter.in.web.RegisterLotRequest;
import com.kb.cosmetic_wms.lot.adapter.in.web.UpdateLotStatusRequest;
import com.kb.cosmetic_wms.lot.application.port.in.LotResult;
import com.kb.cosmetic_wms.lot.application.port.in.RegisterLotCommand;
import com.kb.cosmetic_wms.lot.application.port.in.UpdateLotStatusCommand;
import com.kb.cosmetic_wms.lot.application.service.LotService;
import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import com.kb.cosmetic_wms.lot.domain.exception.DuplicateLotNumberException;
import com.kb.cosmetic_wms.lot.domain.exception.LotErrorCode;
import com.kb.cosmetic_wms.lot.domain.exception.LotNotFoundException;
import com.kb.cosmetic_wms.lot.domain.exception.LotProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.LOT;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LotController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class LotControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LotService lotService;

    // --- 로트 등록 ---

    @Test
    @WithMockUser
    void 올바른_로트_정보를_입력하면_등록에_성공하고_201_Created와_API_문서가_생성된다() throws Exception {
        RegisterLotRequest request = buildRegisterRequest();
        LotResult result = buildResult();
        given(lotService.register(any(RegisterLotCommand.class))).willReturn(result);

        mockMvc.perform(post("/api/v1/lots")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.lotNumber").value("SKN-240101-01-0001"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.productId").value(1L))
                .andDo(document("lot-create-success",
                        buildParams(LOT, "로트 등록", LOT_CREATE_REQUEST, LOT_DETAIL_RESPONSE),
                        createRequestFields(getLotCreateRequestFields()),
                        createResponseFields(getLotDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 로트_번호가_빈_값이면_400_Bad_Request를_반환하고_에러응답이_문서화된다() throws Exception {
        RegisterLotRequest invalidRequest = new RegisterLotRequest(
                "", LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2027, 1, 1, 0, 0), 1L);

        mockMvc.perform(post("/api/v1/lots")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("lot-create-fail-blank-lot-number",
                        buildErrorParams(LOT, "로트 등록"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 로트_번호_형식이_잘못되면_400_Bad_Request를_반환한다() throws Exception {
        RegisterLotRequest invalidRequest = new RegisterLotRequest(
                "skn-240101-01-0001", LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2027, 1, 1, 0, 0), 1L);

        mockMvc.perform(post("/api/v1/lots")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_상품_ID로_로트를_등록하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        RegisterLotRequest request = buildRegisterRequest();
        given(lotService.register(any(RegisterLotCommand.class))).willThrow(new LotProductNotFoundException());

        mockMvc.perform(post("/api/v1/lots")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(LotErrorCode.PRODUCT_NOT_FOUND.getMessage()))
                .andDo(document("lot-create-fail-product-not-found",
                        buildErrorParams(LOT, "로트 등록"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 이미_등록된_로트_번호로_등록하면_409_Conflict를_반환하고_에러응답이_문서화된다() throws Exception {
        RegisterLotRequest request = buildRegisterRequest();
        given(lotService.register(any(RegisterLotCommand.class))).willThrow(new DuplicateLotNumberException());

        mockMvc.perform(post("/api/v1/lots")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_LOT_NUMBER"))
                .andExpect(jsonPath("$.message").value(LotErrorCode.DUPLICATE_LOT_NUMBER.getMessage()))
                .andDo(document("lot-create-fail-duplicate",
                        buildErrorParams(LOT, "로트 등록"),
                        globalErrorResponseFields()
                ));
    }

    // --- 로트 단건 조회 ---

    @Test
    @WithMockUser
    void 존재하는_로트_ID로_조회하면_200_OK와_상세정보를_반환하고_API_문서가_생성된다() throws Exception {
        given(lotService.findById(1L)).willReturn(buildResult());

        mockMvc.perform(get("/api/v1/lots/{lotId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.lotNumber").value("SKN-240101-01-0001"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.manufacturingDate").exists())
                .andExpect(jsonPath("$.expirationDate").exists())
                .andExpect(jsonPath("$.productId").value(1L))
                .andDo(document("lot-get-one-success",
                        buildParams(LOT, "로트 단건 조회", null, LOT_DETAIL_RESPONSE),
                        createResponseFields(getLotDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_로트_ID로_조회하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        given(lotService.findById(999L)).willThrow(new LotNotFoundException());

        mockMvc.perform(get("/api/v1/lots/{lotId}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("LOT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(LotErrorCode.LOT_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("lot-get-one-fail-not-found",
                        buildErrorParams(LOT, "로트 단건 조회"),
                        globalErrorResponseFields()
                ));
    }

    // --- 상품별 로트 목록 조회 ---

    @Test
    @WithMockUser
    void 상품_ID로_로트_목록을_조회하면_200_OK와_전체_목록을_반환하고_API_문서가_생성된다() throws Exception {
        List<LotResult> resultList = List.of(
                buildResult(),
                new LotResult(2L, "SKN-240101-01-0002",
                        LocalDateTime.of(2026, 2, 1, 0, 0),
                        LocalDateTime.of(2027, 2, 1, 0, 0),
                        LotStatus.AVAILABLE, 1L)
        );
        given(lotService.findByProductId(1L)).willReturn(resultList);

        mockMvc.perform(get("/api/v1/lots")
                        .param("productId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].lotNumber").value("SKN-240101-01-0001"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].lotNumber").value("SKN-240101-01-0002"))
                .andDo(document("lot-get-by-product-success",
                        buildParams(LOT, "상품별 로트 목록 조회", null, LOT_DETAIL_RESPONSE),
                        queryParameters(
                                parameterWithName("productId").description("상품 ID (필수)")
                        ),
                        createListResponseFields(getLotDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_상품_ID로_로트_목록을_조회하면_404_Not_Found를_반환한다() throws Exception {
        given(lotService.findByProductId(999L)).willThrow(new LotProductNotFoundException());

        mockMvc.perform(get("/api/v1/lots")
                        .param("productId", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(LotErrorCode.PRODUCT_NOT_FOUND.getMessage()));
    }

    // --- 로트 상태 변경 ---

    @Test
    @WithMockUser
    void 올바른_ID와_상태로_변경하면_200_OK와_갱신된_로트_정보를_반환하고_API_문서가_생성된다() throws Exception {
        UpdateLotStatusRequest request = new UpdateLotStatusRequest(LotStatus.HOLD);
        LotResult result = new LotResult(
                1L, "SKN-240101-01-0001",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2027, 1, 1, 0, 0),
                LotStatus.HOLD, 1L
        );
        given(lotService.updateStatus(eq(1L), any(UpdateLotStatusCommand.class))).willReturn(result);

        mockMvc.perform(patch("/api/v1/lots/{lotId}/status", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("HOLD"))
                .andDo(document("lot-update-status-success",
                        buildParams(LOT, "로트 상태 변경", LOT_STATUS_UPDATE_REQUEST, LOT_DETAIL_RESPONSE),
                        createRequestFields(getLotStatusUpdateRequestFields()),
                        createResponseFields(getLotDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 상태값이_null이면_400_Bad_Request를_반환한다() throws Exception {
        mockMvc.perform(patch("/api/v1/lots/{lotId}/status", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_로트_ID로_상태_변경을_요청하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        UpdateLotStatusRequest request = new UpdateLotStatusRequest(LotStatus.HOLD);
        given(lotService.updateStatus(eq(999L), any(UpdateLotStatusCommand.class)))
                .willThrow(new LotNotFoundException());

        mockMvc.perform(patch("/api/v1/lots/{lotId}/status", 999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("LOT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(LotErrorCode.LOT_NOT_FOUND.getMessage()))
                .andDo(document("lot-update-status-fail-not-found",
                        buildErrorParams(LOT, "로트 상태 변경"),
                        globalErrorResponseFields()
                ));
    }

    // --- 로트 삭제 ---

    @Test
    @WithMockUser
    void 존재하는_로트를_삭제하면_204_No_Content를_반환하고_API_문서가_생성된다() throws Exception {
        doNothing().when(lotService).delete(1L);

        mockMvc.perform(delete("/api/v1/lots/{lotId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(document("lot-delete-success",
                        buildParams(LOT, "로트 삭제")
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_로트_ID로_삭제를_요청하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        doThrow(new LotNotFoundException()).when(lotService).delete(999L);

        mockMvc.perform(delete("/api/v1/lots/{lotId}", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("LOT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(LotErrorCode.LOT_NOT_FOUND.getMessage()))
                .andDo(document("lot-delete-fail-not-found",
                        buildErrorParams(LOT, "로트 삭제"),
                        globalErrorResponseFields()
                ));
    }

    private static RegisterLotRequest buildRegisterRequest() {
        return new RegisterLotRequest(
                "SKN-240101-01-0001",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2027, 1, 1, 0, 0),
                1L
        );
    }

    private static LotResult buildResult() {
        return new LotResult(
                1L,
                "SKN-240101-01-0001",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2027, 1, 1, 0, 0),
                LotStatus.AVAILABLE,
                1L
        );
    }

    private static FieldDescriptor[] getLotCreateRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("lotNumber").type(JsonFieldType.STRING)
                        .description("로트 번호 (형식: [카테고리3자]-[YYMMDD]-[공장2자]-[일련번호4자], 예: SKN-240101-01-0001)"),
                fieldWithPath("manufacturingDate").type(JsonFieldType.STRING)
                        .description("제조일자 (ISO-8601, 예: 2026-01-01T00:00:00)"),
                fieldWithPath("expirationDate").type(JsonFieldType.STRING)
                        .description("유통기한 (ISO-8601, 제조일자 이후여야 함)"),
                fieldWithPath("productId").type(JsonFieldType.NUMBER)
                        .description("연결할 상품 ID (필수)")
        };
    }

    private static FieldDescriptor[] getLotStatusUpdateRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("status").type(JsonFieldType.STRING)
                        .description("변경할 로트 상태 (AVAILABLE / HOLD / EXPIRED / RECALLED / DAMAGED / DISPOSED)")
        };
    }

    private static FieldDescriptor[] getLotDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("로트 고유 식별 번호 (PK)"),
                fieldWithPath("lotNumber").type(JsonFieldType.STRING).description("로트 번호"),
                fieldWithPath("manufacturingDate").type(JsonFieldType.STRING).description("제조일자"),
                fieldWithPath("expirationDate").type(JsonFieldType.STRING).description("유통기한"),
                fieldWithPath("status").type(JsonFieldType.STRING)
                        .description("로트 상태 (AVAILABLE / HOLD / EXPIRED / RECALLED / DAMAGED / DISPOSED)"),
                fieldWithPath("productId").type(JsonFieldType.NUMBER).description("연결된 상품 ID")
        };
    }
}