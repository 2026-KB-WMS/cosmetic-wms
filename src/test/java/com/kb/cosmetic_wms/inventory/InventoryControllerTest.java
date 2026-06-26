package com.kb.cosmetic_wms.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.inventory.adapter.in.web.InventoryController;
import com.kb.cosmetic_wms.inventory.application.port.in.*;
import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryErrorCode;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryNotFoundException;
import com.kb.cosmetic_wms.inventory.fixture.InventoryDtoBuilder;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.INVENTORY;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.INVENTORY_DETAIL_RESPONSE;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.INVENTORY_STATUS_CHANGE_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventoryController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class InventoryControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FindInventoryUseCase findInventoryUseCase;

    @MockitoBean
    private ManageInventoryStatusUseCase manageInventoryStatusUseCase;

    // --- 재고 단건 조회 ---

    @Test
    @WithMockUser
    void 존재하는_재고_ID로_조회하면_200_OK와_상세_정보를_반환하고_API_문서가_생성된다() throws Exception {
        InventoryResult response = buildDetailResult();
        given(findInventoryUseCase.findById(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/inventories/{inventoryId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.lotId").value(10L))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.availableQuantity").value(100))
                .andExpect(jsonPath("$.allocStatus").value("UNALLOCATED"))
                .andExpect(jsonPath("$.qualityStatus").value("NORMAL"))
                .andExpect(jsonPath("$.locStatus").value("STORED"))
                .andDo(document("inventory-get-one-success",
                        buildParams(INVENTORY, "재고 단건 조회", null, INVENTORY_DETAIL_RESPONSE),
                        createResponseFields(getInventoryDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_재고_ID로_조회하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        given(findInventoryUseCase.findById(999L)).willThrow(new InventoryNotFoundException());

        mockMvc.perform(get("/api/v1/inventories/{inventoryId}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("INVENTORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(InventoryErrorCode.INVENTORY_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("inventory-get-one-fail-not-found",
                        buildErrorParams(INVENTORY, "재고 단건 조회"),
                        globalErrorResponseFields()
                ));
    }

    // --- LOT별 목록 조회 ---

    @Test
    @WithMockUser
    void LOT_ID로_재고_목록을_조회하면_200_OK와_목록을_반환하고_API_문서가_생성된다() throws Exception {
        List<InventoryResult> responseList = List.of(buildDetailResult());
        given(findInventoryUseCase.findByLotId(10L)).willReturn(responseList);

        mockMvc.perform(get("/api/v1/inventories")
                        .param("lotId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].lotId").value(10L))
                .andDo(document("inventory-get-by-lot-success",
                        buildParams(INVENTORY, "LOT별 재고 목록 조회", null, INVENTORY_DETAIL_RESPONSE),
                        queryParameters(
                                parameterWithName("lotId").description("조회할 LOT ID")
                        ),
                        createListResponseFields(getInventoryDetailResponseFields())
                ));
    }

    // --- 상품별 목록 조회 ---

    @Test
    @WithMockUser
    void 상품_ID로_재고_목록을_조회하면_200_OK와_목록을_반환하고_API_문서가_생성된다() throws Exception {
        List<InventoryResult> responseList = List.of(buildDetailResult());
        given(findInventoryUseCase.findByProductId(1L)).willReturn(responseList);

        mockMvc.perform(get("/api/v1/inventories")
                        .param("productId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(1L))
                .andDo(document("inventory-get-by-product-success",
                        buildParams(INVENTORY, "상품별 재고 목록 조회", null, INVENTORY_DETAIL_RESPONSE),
                        queryParameters(
                                parameterWithName("productId").description("조회할 상품 ID")
                        ),
                        createListResponseFields(getInventoryDetailResponseFields())
                ));
    }

    // --- 출고 할당 ---

    @Test
    @WithMockUser
    void 유효한_요청으로_출고_할당을_하면_200_OK와_변경된_재고_정보를_반환하고_API_문서가_생성된다() throws Exception {
        InventoryStatusChangeCommand request = new InventoryDtoBuilder().build();
        InventoryResult response = buildAllocatedResult();
        given(manageInventoryStatusUseCase.allocate(eq(1L), any(InventoryStatusChangeCommand.class))).willReturn(response);

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/allocate", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocStatus").value("ALLOCATED"))
                .andExpect(jsonPath("$.availableQuantity").value(0))
                .andDo(document("inventory-allocate-success",
                        buildParams(INVENTORY, "출고 할당", INVENTORY_STATUS_CHANGE_REQUEST, INVENTORY_DETAIL_RESPONSE),
                        createRequestFields(getStatusChangeRequestFields()),
                        createResponseFields(getInventoryDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 수량이_0이면_400_Bad_Request를_반환한다() throws Exception {
        InventoryStatusChangeCommand invalidRequest = new InventoryDtoBuilder().quantity(0).build();

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/allocate", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_재고_ID로_출고_할당하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        InventoryStatusChangeCommand request = new InventoryDtoBuilder().build();
        given(manageInventoryStatusUseCase.allocate(eq(999L), any())).willThrow(new InventoryNotFoundException());

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/allocate", 999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("INVENTORY_NOT_FOUND"))
                .andDo(document("inventory-allocate-fail-not-found",
                        buildErrorParams(INVENTORY, "출고 할당"),
                        globalErrorResponseFields()
                ));
    }

    // --- 할당 취소 ---

    @Test
    @WithMockUser
    void 유효한_요청으로_할당_취소하면_200_OK와_변경된_재고_정보를_반환하고_API_문서가_생성된다() throws Exception {
        InventoryStatusChangeCommand request = new InventoryDtoBuilder().build();
        InventoryResult response = buildDetailResult();
        given(manageInventoryStatusUseCase.unallocate(eq(1L), any(InventoryStatusChangeCommand.class))).willReturn(response);

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/unallocate", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocStatus").value("UNALLOCATED"))
                .andDo(document("inventory-unallocate-success",
                        buildParams(INVENTORY, "할당 취소", INVENTORY_STATUS_CHANGE_REQUEST, INVENTORY_DETAIL_RESPONSE),
                        createRequestFields(getStatusChangeRequestFields()),
                        createResponseFields(getInventoryDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_재고_ID로_할당_취소하면_404_Not_Found를_반환한다() throws Exception {
        InventoryStatusChangeCommand request = new InventoryDtoBuilder().build();
        given(manageInventoryStatusUseCase.unallocate(eq(999L), any())).willThrow(new InventoryNotFoundException());

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/unallocate", 999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("INVENTORY_NOT_FOUND"));
    }

    // --- 위치 이동 ---

    @Test
    @WithMockUser
    void 유효한_요청으로_이동_시작하면_200_OK와_MOVING_상태의_재고_정보를_반환하고_API_문서가_생성된다() throws Exception {
        InventoryStatusChangeCommand request = new InventoryDtoBuilder().buildWithoutRef();
        InventoryResult response = buildMovingResult();
        given(manageInventoryStatusUseCase.startMoving(eq(1L), any(InventoryStatusChangeCommand.class))).willReturn(response);

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/move/start", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locStatus").value("MOVING"))
                .andDo(document("inventory-move-start-success",
                        buildParams(INVENTORY, "이동 시작", INVENTORY_STATUS_CHANGE_REQUEST, INVENTORY_DETAIL_RESPONSE),
                        createRequestFields(getStatusChangeRequestFields()),
                        createResponseFields(getInventoryDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 유효한_요청으로_이동_완료하면_200_OK와_STORED_상태의_재고_정보를_반환하고_API_문서가_생성된다() throws Exception {
        InventoryStatusChangeCommand request = new InventoryDtoBuilder().buildWithoutRef();
        InventoryResult response = buildDetailResult();
        given(manageInventoryStatusUseCase.finishMoving(eq(1L), any(InventoryStatusChangeCommand.class))).willReturn(response);

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/move/finish", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locStatus").value("STORED"))
                .andDo(document("inventory-move-finish-success",
                        buildParams(INVENTORY, "이동 완료", INVENTORY_STATUS_CHANGE_REQUEST, INVENTORY_DETAIL_RESPONSE),
                        createRequestFields(getStatusChangeRequestFields()),
                        createResponseFields(getInventoryDetailResponseFields())
                ));
    }

    // --- 품질 상태 관리 ---

    @Test
    @WithMockUser
    void 유효한_요청으로_검수_시작하면_200_OK와_INSPECTING_상태의_재고_정보를_반환하고_API_문서가_생성된다() throws Exception {
        InventoryStatusChangeCommand request = new InventoryDtoBuilder().buildWithoutRef();
        InventoryResult response = buildInspectingResult();
        given(manageInventoryStatusUseCase.startInspecting(eq(1L), any(InventoryStatusChangeCommand.class))).willReturn(response);

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/quality/inspect", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qualityStatus").value("INSPECTING"))
                .andDo(document("inventory-quality-inspect-success",
                        buildParams(INVENTORY, "품질 검수 시작", INVENTORY_STATUS_CHANGE_REQUEST, INVENTORY_DETAIL_RESPONSE),
                        createRequestFields(getStatusChangeRequestFields()),
                        createResponseFields(getInventoryDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 유효한_요청으로_품질_정상_복귀하면_200_OK와_NORMAL_상태의_재고_정보를_반환하고_API_문서가_생성된다() throws Exception {
        InventoryStatusChangeCommand request = new InventoryDtoBuilder().buildWithoutRef();
        InventoryResult response = buildDetailResult();
        given(manageInventoryStatusUseCase.restoreToNormalQuality(eq(1L), any(InventoryStatusChangeCommand.class))).willReturn(response);

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/quality/restore", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qualityStatus").value("NORMAL"))
                .andDo(document("inventory-quality-restore-success",
                        buildParams(INVENTORY, "품질 정상 복귀", INVENTORY_STATUS_CHANGE_REQUEST, INVENTORY_DETAIL_RESPONSE),
                        createRequestFields(getStatusChangeRequestFields()),
                        createResponseFields(getInventoryDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 유효한_요청으로_품질_보류하면_200_OK와_HOLD_상태의_재고_정보를_반환하고_API_문서가_생성된다() throws Exception {
        InventoryStatusChangeCommand request = new InventoryDtoBuilder().buildWithoutRef();
        InventoryResult response = buildHoldResult();
        given(manageInventoryStatusUseCase.holdForQualityIssue(eq(1L), any(InventoryStatusChangeCommand.class))).willReturn(response);

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/quality/hold", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qualityStatus").value("HOLD"))
                .andDo(document("inventory-quality-hold-success",
                        buildParams(INVENTORY, "품질 보류", INVENTORY_STATUS_CHANGE_REQUEST, INVENTORY_DETAIL_RESPONSE),
                        createRequestFields(getStatusChangeRequestFields()),
                        createResponseFields(getInventoryDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 유효한_요청으로_폐기_예정_처리하면_200_OK와_DISCARD_SCHEDULED_상태의_재고_정보를_반환하고_API_문서가_생성된다() throws Exception {
        InventoryStatusChangeCommand request = new InventoryDtoBuilder().buildWithoutRef();
        InventoryResult response = buildDiscardResult();
        given(manageInventoryStatusUseCase.scheduleForDiscard(eq(1L), any(InventoryStatusChangeCommand.class))).willReturn(response);

        mockMvc.perform(patch("/api/v1/inventories/{inventoryId}/quality/discard", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qualityStatus").value("DISCARD_SCHEDULED"))
                .andDo(document("inventory-quality-discard-success",
                        buildParams(INVENTORY, "폐기 예정 처리", INVENTORY_STATUS_CHANGE_REQUEST, INVENTORY_DETAIL_RESPONSE),
                        createRequestFields(getStatusChangeRequestFields()),
                        createResponseFields(getInventoryDetailResponseFields())
                ));
    }

    // --- Result builders ---

    private static final java.time.LocalDate EXPIRY = java.time.LocalDate.of(2026, 12, 31);

    private static InventoryResult buildDetailResult() {
        return new InventoryResult(
                1L, 1L, 10L, 1000L, 100L,
                100, 100,
                AllocStatus.UNALLOCATED, QualityStatus.NORMAL, LocStatus.STORED, EXPIRY
        );
    }

    private static InventoryResult buildAllocatedResult() {
        return new InventoryResult(
                1L, 1L, 10L, 1000L, 100L,
                30, 0,
                AllocStatus.ALLOCATED, QualityStatus.NORMAL, LocStatus.STORED, EXPIRY
        );
    }

    private static InventoryResult buildMovingResult() {
        return new InventoryResult(
                1L, 1L, 10L, 1000L, 100L,
                30, 0,
                AllocStatus.UNALLOCATED, QualityStatus.NORMAL, LocStatus.MOVING, EXPIRY
        );
    }

    private static InventoryResult buildInspectingResult() {
        return new InventoryResult(
                1L, 1L, 10L, 1000L, 100L,
                30, 0,
                AllocStatus.UNALLOCATED, QualityStatus.INSPECTING, LocStatus.STORED, EXPIRY
        );
    }

    private static InventoryResult buildHoldResult() {
        return new InventoryResult(
                1L, 1L, 10L, 1000L, 100L,
                30, 0,
                AllocStatus.UNALLOCATED, QualityStatus.HOLD, LocStatus.STORED, EXPIRY
        );
    }

    private static InventoryResult buildDiscardResult() {
        return new InventoryResult(
                1L, 1L, 10L, 1000L, 100L,
                30, 0,
                AllocStatus.UNALLOCATED, QualityStatus.DISCARD_SCHEDULED, LocStatus.STORED, EXPIRY
        );
    }

    // --- Field descriptors ---

    private static FieldDescriptor[] getStatusChangeRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("quantity").type(JsonFieldType.NUMBER)
                        .description("처리 대상 수량 (1 이상의 양수)"),
                fieldWithPath("referenceId").type(JsonFieldType.NUMBER)
                        .description("연관 참조 ID (ALLOCATE·UNALLOCATE 시 주문 ID 필수, 나머지 선택)").optional(),
                fieldWithPath("memberId").type(JsonFieldType.NUMBER)
                        .description("작업자 회원 ID (필수)")
        };
    }

    private static FieldDescriptor[] getInventoryDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("재고 고유 식별 번호 (PK)"),
                fieldWithPath("productId").type(JsonFieldType.NUMBER).description("연결된 상품 ID"),
                fieldWithPath("lotId").type(JsonFieldType.NUMBER).description("연결된 LOT ID"),
                fieldWithPath("sectionId").type(JsonFieldType.NUMBER).description("보관 구역 ID"),
                fieldWithPath("warehouseId").type(JsonFieldType.NUMBER).description("보관 창고 ID"),
                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("전체 수량"),
                fieldWithPath("availableQuantity").type(JsonFieldType.NUMBER).description("출고 가용 수량"),
                fieldWithPath("allocStatus").type(JsonFieldType.STRING)
                        .description("할당 상태 (UNALLOCATED / ALLOCATED)"),
                fieldWithPath("qualityStatus").type(JsonFieldType.STRING)
                        .description("품질 상태 (NORMAL / INSPECTING / HOLD / DISCARD_SCHEDULED)"),
                fieldWithPath("locStatus").type(JsonFieldType.STRING)
                        .description("위치 상태 (STORED / MOVING)"),
                fieldWithPath("expiryDate").type(JsonFieldType.STRING)
                        .description("유통기한 (yyyy-MM-dd)")
        };
    }
}
