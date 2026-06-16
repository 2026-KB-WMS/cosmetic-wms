package com.kb.cosmetic_wms.domain.storage;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.dto.SectionCreateRequestDto;
import com.kb.cosmetic_wms.domain.storage.dto.WarehouseCreateRequestDto;
import com.kb.cosmetic_wms.domain.storage.dto.WarehouseResponseDto;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;
import com.kb.cosmetic_wms.domain.storage.exception.DuplicateWarehouseException;
import com.kb.cosmetic_wms.domain.storage.exception.StorageErrorCode;
import com.kb.cosmetic_wms.domain.storage.exception.StorageExceedCapacityException;
import com.kb.cosmetic_wms.domain.storage.exception.WarehouseNotFoundException;
import com.kb.cosmetic_wms.domain.storage.fixture.WarehouseTestBuilder;
import com.kb.cosmetic_wms.domain.storage.repository.WarehouseRepository;
import com.kb.cosmetic_wms.domain.storage.service.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {

    @InjectMocks
    private StorageService storageService;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Test
    void 올바른_창고_정보를_입력하면_정상적으로_데이터에_등록된다() {
        // given
        WarehouseCreateRequestDto requestDto = new WarehouseCreateRequestDto(
                "평택 냉동 허브", "경기도 평택시", "10~20도", 50000);
        Warehouse warehouse = new WarehouseTestBuilder()
                .warehouseName(requestDto.warehouseName())
                .address(requestDto.address())
                .targetTemp(requestDto.targetTemp())
                .capacity(requestDto.capacity())
                .build();

        given(warehouseRepository.save(any(Warehouse.class))).willReturn(warehouse);

        // when
        WarehouseResponseDto response = storageService.registerWarehouse(requestDto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.warehouseName()).isEqualTo("평택 냉동 허브");
    }

    @Test
    void 이미_동일한_이름과_주소로_등록된_창고가_존재하면_DuplicateWarehouseException을_던진다() {
        // given
        WarehouseCreateRequestDto requestDto = new WarehouseCreateRequestDto(
                "인천 냉동 허브", "인천광역시 중구", "0~5도", 20000);

        given(warehouseRepository.existsByWarehouseNameAndAddress(requestDto.warehouseName(), requestDto.address()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> storageService.registerWarehouse(requestDto))
                .isInstanceOf(DuplicateWarehouseException.class)
                .hasMessage(StorageErrorCode.DUPLICATE_WAREHOUSE.getMessage());
    }

    @Test
    void 특정_창고에_하위_섹션을_추가할_때_전체_섹션_용량_합이_창고_허용_용량을_초과하면_ExceedCapacityException을_던진다() {
        // given
        Long warehouseId = 1L;
        Warehouse warehouse = new WarehouseTestBuilder()
                .capacity(5000)
                .build();
        warehouse.addStorageSection(
                "WH03-HIGH-R-01", "기존 구역", SectionType.HIGH_ROT, TemperatureType.ROOM, 4000);

        given(warehouseRepository.findById(warehouseId)).willReturn(Optional.of(warehouse));

        SectionCreateRequestDto overflowRequest = new SectionCreateRequestDto(
                SectionType.MID_ROT, "WH03-MID-R-02", "초과 구역", TemperatureType.ROOM, 2000
        );

        // when & then
        assertThatThrownBy(() -> storageService.addSectionToWarehouse(warehouseId, overflowRequest))
                .isInstanceOf(StorageExceedCapacityException.class)
                .hasMessage(StorageErrorCode.EXCEED_WAREHOUSE_CAPACITY.getMessage());
    }

    @Test
    void 창고_전체_목록을_요청하면_저장소의_모든_데이터가_DTO_리스트로_치환되어_반환된다() {
        // given
        Warehouse wh1 = new WarehouseTestBuilder().warehouseName("제1창고").build();
        Warehouse wh2 = new WarehouseTestBuilder().warehouseName("제2창고").build();
        given(warehouseRepository.findAll()).willReturn(List.of(wh1, wh2));

        // when
        List<WarehouseResponseDto> results = storageService.getAllWarehouses();

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).warehouseName()).isEqualTo("제1창고");
        assertThat(results.get(1).warehouseName()).isEqualTo("제2창고");
    }

    @Test
    void 존하지_않는_창고_ID로_단건_조회를_시도하면_WarehouseNotFoundException이_발생한다() {
        // given
        Long invalidWarehouseId = 99L;
        given(warehouseRepository.findById(invalidWarehouseId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storageService.getWarehouseById(invalidWarehouseId))
                .isInstanceOf(WarehouseNotFoundException.class);
    }
}
