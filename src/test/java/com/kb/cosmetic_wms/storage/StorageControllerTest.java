package com.kb.cosmetic_wms.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import com.kb.cosmetic_wms.storage.adapter.in.web.SectionCreateRequest;
import com.kb.cosmetic_wms.storage.adapter.in.web.StorageController;
import com.kb.cosmetic_wms.storage.adapter.in.web.WarehouseCreateRequest;
import com.kb.cosmetic_wms.storage.application.port.in.*;
import com.kb.cosmetic_wms.storage.domain.exception.DuplicateWarehouseException;
import com.kb.cosmetic_wms.storage.domain.exception.StorageExceedCapacityException;
import com.kb.cosmetic_wms.storage.domain.exception.WarehouseNotFoundException;
import com.kb.cosmetic_wms.storage.domain.model.SectionAllocationStatus;
import com.kb.cosmetic_wms.storage.domain.model.SectionQualityStatus;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.STORAGE;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StorageController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class StorageControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterWarehouseUseCase registerWarehouseUseCase;

    @MockitoBean
    private AddSectionUseCase addSectionUseCase;

    @MockitoBean
    private FindWarehouseUseCase findWarehouseUseCase;

    @Test
    @WithMockUser
    void 올바른_창고_정보를_입력하면_등록에_성공하고_API_문서가_생성된다() throws Exception {
        WarehouseCreateRequest request = new WarehouseCreateRequest("용인 신선 센터", "경기도 용인시", "0~5도", 30000);
        WarehouseResult result = new WarehouseResult(1L, "용인 신선 센터", "경기도 용인시", "0~5도", 30000, List.of());

        given(registerWarehouseUseCase.register(any(RegisterWarehouseCommand.class))).willReturn(result);

        mockMvc.perform(post("/api/v1/storages/warehouses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warehouseId").value(1L))
                .andDo(document("warehouse-create-success",
                        buildParams(STORAGE, "창고 등록", WAREHOUSE_CREATE_REQUEST, WAREHOUSE_RESPONSE),
                        createRequestFields(getWarehouseCreateRequestFields()),
                        getWarehouseResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 창고명과_주소가_중복되면_400_에러를_반환하고_에러응답이_문서화된다() throws Exception {
        WarehouseCreateRequest request = new WarehouseCreateRequest("중복 창고", "서울시 강남구", "10~20도", 10000);
        given(registerWarehouseUseCase.register(any(RegisterWarehouseCommand.class)))
                .willThrow(new DuplicateWarehouseException());

        mockMvc.perform(post("/api/v1/storages/warehouses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_WAREHOUSE"))
                .andDo(document("warehouse-create-fail-duplicate",
                        buildErrorParams(STORAGE, "창고 등록"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 특정_창고에_보관구역_섹션을_정상_추가하면_200_OK와_함께_업데이트된_창고정보가_반환된다() throws Exception {
        Long warehouseId = 1L;
        SectionCreateRequest request = new SectionCreateRequest(
                SectionType.STORAGE, "A동 상단랙", TemperatureZone.ROOM, 5000);

        SectionResult sectionResult = new SectionResult(
                1L, "WH01-STR-R-01", "A동 상단랙",
                SectionType.STORAGE, SectionQualityStatus.NORMAL, SectionAllocationStatus.AVAILABLE,
                TemperatureZone.ROOM, 5000, 0);
        WarehouseResult warehouseResult = new WarehouseResult(
                warehouseId, "용인 신선 센터", "경기도 용인시", "0~5도", 30000, List.of(sectionResult));

        given(addSectionUseCase.addSection(eq(warehouseId), any(AddSectionCommand.class))).willReturn(warehouseResult);

        mockMvc.perform(post("/api/v1/storages/warehouses/{warehouseId}/sections", warehouseId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections[0].sectionCode").value("WH01-STR-R-01"))
                .andDo(document("section-add-success",
                        buildParams(STORAGE, "창고 하위 구역 편입", SECTION_CREATE_REQUEST, WAREHOUSE_RESPONSE),
                        createRequestFields(getSectionCreateRequestFields()),
                        getWarehouseResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 구역_편입_시_허용_용량을_초과하면_400_에러를_반환하고_문서화된다() throws Exception {
        Long warehouseId = 1L;
        SectionCreateRequest request = new SectionCreateRequest(
                SectionType.STORAGE, "초과 구역", TemperatureZone.ROOM, 40000);
        given(addSectionUseCase.addSection(eq(warehouseId), any(AddSectionCommand.class)))
                .willThrow(new StorageExceedCapacityException());

        mockMvc.perform(post("/api/v1/storages/warehouses/{warehouseId}/sections", warehouseId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("EXCEED_WAREHOUSE_CAPACITY"))
                .andDo(document("section-add-fail-exceed-capacity",
                        buildErrorParams(STORAGE, "창고 하위 구역 편입"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 창고_전체_목록_조회_성공_시_200_OK와_함께_스니펫_문서가_추출된다() throws Exception {
        WarehouseResult wh1 = new WarehouseResult(1L, "평택 센터", "경기도 평택시", "10~20도", 50000, List.of());
        WarehouseResult wh2 = new WarehouseResult(2L, "인천 센터", "인천광역시 중구", "0~5도", 30000, List.of());
        given(findWarehouseUseCase.findAll()).willReturn(List.of(wh1, wh2));

        mockMvc.perform(get("/api/v1/storages/warehouses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].warehouseName").value("평택 센터"))
                .andDo(document("warehouse-get-all-success",
                        buildParams(STORAGE, "창고 전체 목록 조회", null, WAREHOUSE_RESPONSE),
                        getWarehouseListResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_창고ID를_단건_조회하면_404_에러를_리턴하고_문서화된다() throws Exception {
        Long invalidId = 99L;
        given(findWarehouseUseCase.findById(invalidId)).willThrow(new WarehouseNotFoundException());

        mockMvc.perform(get("/api/v1/storages/warehouses/{warehouseId}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STORAGE_NOT_FOUND"))
                .andDo(document("warehouse-get-one-fail-not-found",
                        buildErrorParams(STORAGE, "창고 단건 조회"),
                        globalErrorResponseFields()
                ));
    }

    private static ResponseFieldsSnippet getWarehouseResponseFields() {
        return createResponseFields(getWarehouseFieldDescriptors());
    }

    private static ResponseFieldsSnippet getWarehouseListResponseFields() {
        return createListResponseFields(getWarehouseFieldDescriptors());
    }

    private static FieldDescriptor[] getWarehouseCreateRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("warehouseName").description("창고 상호 명칭 (필수값)"),
                fieldWithPath("address").description("창고 물리 소재지 주소 (필수값)"),
                fieldWithPath("targetTemp").description("설정 보관 온도 범위 (포맷: 숫자~숫자도)"),
                fieldWithPath("capacity").description("창고 총 수용 허용 용량")
        };
    }

    private static FieldDescriptor[] getSectionCreateRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("sectionType").description("섹션 종류 (STORAGE, DOCKING, QUARANTINE)"),
                fieldWithPath("sectionName").description("보관 섹션 세부 명칭"),
                fieldWithPath("temperatureType").description("섹션 관리 온도 타입 (ROOM, COOL)"),
                fieldWithPath("maxCapacity").description("해당 구역의 최대 수용 가능 용량")
        };
    }

    private static FieldDescriptor[] getWarehouseFieldDescriptors() {
        return new FieldDescriptor[]{
                fieldWithPath("warehouseId").description("창고 고유 식별 번호 (PK)"),
                fieldWithPath("warehouseName").description("창고 명칭"),
                fieldWithPath("address").description("창고 주소"),
                fieldWithPath("targetTemp").description("창고 타겟 온도"),
                fieldWithPath("capacity").description("창고 총 허용 캐파 수치"),

                fieldWithPath("sections").type(JsonFieldType.ARRAY).description("창고에 소속된 하위 구역 리스트 배열"),

                fieldWithPath("sections[].id").type(JsonFieldType.NUMBER).description("섹션 고유 식별 번호 (PK)").optional(),
                fieldWithPath("sections[].sectionCode").type(JsonFieldType.STRING).description("시스템 자동 발급 섹션 코드 (예: WH01-HIGH-R-01)").optional(),
                fieldWithPath("sections[].sectionName").type(JsonFieldType.STRING).description("섹션 지정 명칭").optional(),
                fieldWithPath("sections[].sectionType").type(JsonFieldType.STRING).description("섹션 도메인 타입").optional(),
                fieldWithPath("sections[].qualityStatus").type(JsonFieldType.STRING).description("품질 상태 정보 (NORMAL, INSPECTING, HOLD)").optional(),
                fieldWithPath("sections[].allocationStatus").type(JsonFieldType.STRING).description("재고 할당 가용 상태 (AVAILABLE, NONE, EXCLUDED)").optional(),
                fieldWithPath("sections[].temperatureType").type(JsonFieldType.STRING).description("섹션 온도 속성").optional(),
                fieldWithPath("sections[].maxCapacity").type(JsonFieldType.NUMBER).description("섹션 한도 용량 크기").optional(),
                fieldWithPath("sections[].currentCapacity").type(JsonFieldType.NUMBER).description("현재 섹션에 적재된 실시간 재고 용량 수치").optional()
        };
    }
}