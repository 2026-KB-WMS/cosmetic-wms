package com.kb.cosmetic_wms.storage;

import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.storage.fixture.WarehouseTestBuilder;
import com.kb.cosmetic_wms.storage.application.port.in.*;
import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import com.kb.cosmetic_wms.storage.application.service.StorageService;
import com.kb.cosmetic_wms.storage.domain.exception.DuplicateWarehouseException;
import com.kb.cosmetic_wms.storage.domain.exception.StorageErrorCode;
import com.kb.cosmetic_wms.storage.domain.exception.StorageExceedCapacityException;
import com.kb.cosmetic_wms.storage.domain.exception.WarehouseNotFoundException;
import com.kb.cosmetic_wms.storage.domain.model.SectionCode;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;
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
    private StoragePort storagePort;

    @Test
    void 올바른_창고_정보를_입력하면_정상적으로_데이터에_등록된다() {
        RegisterWarehouseCommand command = new RegisterWarehouseCommand("평택 냉동 허브", "경기도 평택시", "10~20도", 50000);
        Warehouse warehouse = new WarehouseTestBuilder()
                .warehouseName(command.warehouseName())
                .address(command.address())
                .targetTemp(command.targetTemp())
                .capacity(command.capacity())
                .build();

        given(storagePort.save(any(Warehouse.class))).willReturn(warehouse);

        WarehouseResult result = storageService.register(command);

        assertThat(result).isNotNull();
        assertThat(result.warehouseName()).isEqualTo("평택 냉동 허브");
    }

    @Test
    void 이미_동일한_이름과_주소로_등록된_창고가_존재하면_DuplicateWarehouseException을_던진다() {
        RegisterWarehouseCommand command = new RegisterWarehouseCommand("인천 냉동 허브", "인천광역시 중구", "0~5도", 20000);

        given(storagePort.existsByNameAndAddress(command.warehouseName(), command.address())).willReturn(true);

        assertThatThrownBy(() -> storageService.register(command))
                .isInstanceOf(DuplicateWarehouseException.class)
                .hasMessage(StorageErrorCode.DUPLICATE_WAREHOUSE.getMessage());
    }

    @Test
    void 특정_창고에_하위_섹션을_추가할_때_전체_섹션_용량_합이_창고_허용_용량을_초과하면_ExceedCapacityException을_던진다() {
        Long warehouseId = 1L;
        Warehouse warehouse = new WarehouseTestBuilder().warehouseId(warehouseId).capacity(5000).build();
        warehouse.addStorageSection(new SectionCode("WH01-STR-R-01"), "기존 구역",
                SectionType.STORAGE, TemperatureZone.ROOM, 4000);

        given(storagePort.findByIdForUpdate(warehouseId)).willReturn(Optional.of(warehouse));

        AddSectionCommand overflowCommand = new AddSectionCommand(
                SectionType.STORAGE, "초과 구역", TemperatureZone.ROOM, 2000
        );

        assertThatThrownBy(() -> storageService.addSection(warehouseId, overflowCommand))
                .isInstanceOf(StorageExceedCapacityException.class)
                .hasMessage(StorageErrorCode.EXCEED_WAREHOUSE_CAPACITY.getMessage());
    }

    @Test
    void 창고_전체_목록을_요청하면_저장소의_모든_데이터가_DTO_리스트로_치환되어_반환된다() {
        Warehouse wh1 = new WarehouseTestBuilder().warehouseName("제1창고").build();
        Warehouse wh2 = new WarehouseTestBuilder().warehouseName("제2창고").build();
        given(storagePort.findAll()).willReturn(List.of(wh1, wh2));

        List<WarehouseResult> results = storageService.findAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).warehouseName()).isEqualTo("제1창고");
        assertThat(results.get(1).warehouseName()).isEqualTo("제2창고");
    }

    @Test
    void 존재하지_않는_창고_ID로_단건_조회를_시도하면_WarehouseNotFoundException이_발생한다() {
        Long invalidWarehouseId = 99L;
        given(storagePort.findById(invalidWarehouseId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> storageService.findById(invalidWarehouseId))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

}