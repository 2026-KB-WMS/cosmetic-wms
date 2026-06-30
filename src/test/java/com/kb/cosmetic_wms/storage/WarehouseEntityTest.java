package com.kb.cosmetic_wms.storage;

import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.storage.fixture.SectionTestBuilder;
import com.kb.cosmetic_wms.storage.fixture.WarehouseTestBuilder;
import com.kb.cosmetic_wms.storage.domain.exception.DuplicateSectionCodeException;
import com.kb.cosmetic_wms.storage.domain.exception.SectionCapacityOverflowException;
import com.kb.cosmetic_wms.storage.domain.exception.SectionCapacityUnderflowException;
import com.kb.cosmetic_wms.storage.domain.exception.StorageErrorCode;
import com.kb.cosmetic_wms.storage.domain.exception.StorageExceedCapacityException;
import com.kb.cosmetic_wms.storage.domain.exception.StorageValidationException;
import com.kb.cosmetic_wms.storage.domain.model.Section;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;

import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class WarehouseEntityTest {

    @Test
    void 올바른_정보를_입력하면_창고_객체가_정상_생성된다() {
        Warehouse warehouse = new WarehouseTestBuilder().build();

        assertThat(warehouse.getWarehouseName()).isEqualTo("인천 제1 센터");
        assertThat(warehouse.getAddress()).isEqualTo("인천광역시 중구");
        assertThat(warehouse.getCapacity()).isEqualTo(10000);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 창고_이름이_null이거나_공백이면_예외를_던진다(String invalidWarehouseName) {
        assertThatThrownBy(() ->
                new WarehouseTestBuilder().warehouseName(invalidWarehouseName).build()
        )
                .isInstanceOf(StorageValidationException.class)
                .hasMessage(StorageErrorCode.INVALID_WAREHOUSE_NAME.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 창고_주소가_null이거나_공백이면_예외를_던진다(String invalidAddress) {
        assertThatThrownBy(() ->
                new WarehouseTestBuilder().address(invalidAddress).build()
        )
                .isInstanceOf(StorageValidationException.class)
                .hasMessage(StorageErrorCode.INVALID_ADDRESS.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "10~25", "10-25도", "상온", "abcdefg"})
    void 창고_적정_온도가_올바르지_않으면_예외를_던진다(String invalidTemp) {
        assertThatThrownBy(() ->
                new WarehouseTestBuilder().targetTemp(invalidTemp).build()
        )
                .hasMessage(StorageErrorCode.INVALID_TARGET_TEMP.getMessage());
    }

    @Test
    void 창고_수용_한도가_0_이하이면_예외를_던진다() {
        Assertions.assertThatThrownBy(() ->
                        new WarehouseTestBuilder().capacity(0).build()
                )
                .isInstanceOf(StorageValidationException.class)
                .hasMessage(StorageErrorCode.INVALID_CAPACITY.getMessage());
    }

    @Test
    void 추가하려는_섹션들의_최대_수용량_합이_창고_전체_수용_한도를_초과하면_예외를_던진다() {
        Warehouse warehouse = new WarehouseTestBuilder().capacity(10000).build();

        new SectionTestBuilder().warehouse(warehouse).sectionCode("WH01-STR-R-01").maxCapacity(6000).build();

        assertThatThrownBy(() ->
                new SectionTestBuilder().warehouse(warehouse).sectionCode("WH01-STR-R-02").maxCapacity(6000).build()
        )
                .isInstanceOf(StorageExceedCapacityException.class)
                .hasMessage(StorageErrorCode.EXCEED_WAREHOUSE_CAPACITY.getMessage());
    }

    @Test
    void 창고_내에_동일한_코드를_가진_섹션이_이미_존재하면_예외를_던진다() {
        Warehouse warehouse = new WarehouseTestBuilder().build();

        new SectionTestBuilder().warehouse(warehouse).sectionCode("WH01-STR-R-01").build();

        assertThatThrownBy(() ->
                new SectionTestBuilder().warehouse(warehouse).sectionCode("WH01-STR-R-01").build()
        )
                .isInstanceOf(DuplicateSectionCodeException.class)
                .hasMessage(StorageErrorCode.DUPLICATE_SECTION_CODE.getMessage());
    }

    @Test
    void 규격에_맞는_보관_구역들을_추가하면_창고에_정상_등록된다() {
        Warehouse warehouse = new WarehouseTestBuilder().capacity(10000).build();

        Section dockingSection = new SectionTestBuilder()
                .warehouse(warehouse).sectionCode("WH01-DOCK-R-01").sectionType(SectionType.DOCKING).maxCapacity(2000).build();
        Section storageSection = new SectionTestBuilder()
                .warehouse(warehouse).sectionCode("WH01-STR-C-01").sectionType(SectionType.STORAGE)
                .temperatureType(TemperatureZone.COOL).maxCapacity(5000).build();

        assertThat(warehouse.getSections()).hasSize(2);
        assertThat(dockingSection.getSectionCode().value()).isEqualTo("WH01-DOCK-R-01");
        assertThat(storageSection.getSectionCode().value()).isEqualTo("WH01-STR-C-01");
    }

    @Test
    void 추가하려는_섹션들의_용량_합이_창고_전체_한도와_일치하면_예외_없이_정상_등록된다() {
        Warehouse warehouse = new WarehouseTestBuilder().capacity(10000).build();
        new SectionTestBuilder().warehouse(warehouse).sectionCode("WH01-STR-R-01").maxCapacity(7000).build();

        assertDoesNotThrow(() ->
                new SectionTestBuilder().warehouse(warehouse).sectionCode("WH01-STR-R-02").maxCapacity(3000).build()
        );

        assertThat(warehouse.getSections()).hasSize(2);
    }

    @Test
    void DOCKING_섹션에_수령_수량을_반영하면_currentCapacity가_증가한다() {
        Warehouse warehouse = new WarehouseTestBuilder().warehouseId(1L).capacity(10000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-DOCK-R-01").sectionType(SectionType.DOCKING)
                .temperatureType(TemperatureZone.ROOM).maxCapacity(3000).build();

        warehouse.receiveToDocking(Map.of(TemperatureZone.ROOM, 500));

        Section docking = warehouse.getSections().get(0);
        assertThat(docking.getCurrentCapacity()).isEqualTo(500);
    }

    @Test
    void 여러_온도대의_DOCKING_섹션에_수령_수량을_반영하면_각_온도대별로_currentCapacity가_증가한다() {
        Warehouse warehouse = new WarehouseTestBuilder().warehouseId(1L).capacity(10000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-DOCK-R-01").sectionType(SectionType.DOCKING)
                .temperatureType(TemperatureZone.ROOM).maxCapacity(3000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-DOCK-C-01").sectionType(SectionType.DOCKING)
                .temperatureType(TemperatureZone.COOL).maxCapacity(2000).build();

        warehouse.receiveToDocking(Map.of(TemperatureZone.ROOM, 300, TemperatureZone.COOL, 200));

        assertThat(warehouse.getSections().get(0).getCurrentCapacity()).isEqualTo(300);
        assertThat(warehouse.getSections().get(1).getCurrentCapacity()).isEqualTo(200);
    }

    @Test
    void DOCKING_수용량을_초과하는_수령_수량이_들어오면_SectionCapacityOverflowException이_발생한다() {
        Warehouse warehouse = new WarehouseTestBuilder().warehouseId(1L).capacity(10000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-DOCK-R-01").sectionType(SectionType.DOCKING)
                .temperatureType(TemperatureZone.ROOM).maxCapacity(100).build();

        assertThatThrownBy(() -> warehouse.receiveToDocking(Map.of(TemperatureZone.ROOM, 200)))
                .isInstanceOf(SectionCapacityOverflowException.class)
                .hasMessage(StorageErrorCode.SECTION_CAPACITY_OVERFLOW.getMessage());
    }

    @Test
    void 해당_온도대의_DOCKING_섹션이_없으면_SectionCapacityOverflowException이_발생한다() {
        Warehouse warehouse = new WarehouseTestBuilder().warehouseId(1L).capacity(10000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-DOCK-R-01").sectionType(SectionType.DOCKING)
                .temperatureType(TemperatureZone.ROOM).maxCapacity(3000).build();

        assertThatThrownBy(() -> warehouse.receiveToDocking(Map.of(TemperatureZone.COOL, 100)))
                .isInstanceOf(SectionCapacityOverflowException.class);
    }

    @Test
    void DOCKING_섹션에서_검사_완료_수량을_차감하면_currentCapacity가_감소한다() {
        Warehouse warehouse = new WarehouseTestBuilder().warehouseId(1L).capacity(10000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-DOCK-R-01").sectionType(SectionType.DOCKING)
                .temperatureType(TemperatureZone.ROOM).maxCapacity(3000).build();
        warehouse.receiveToDocking(Map.of(TemperatureZone.ROOM, 500));

        warehouse.releaseFromDocking(Map.of(TemperatureZone.ROOM, 300));

        assertThat(warehouse.getSections().get(0).getCurrentCapacity()).isEqualTo(200);
    }

    @Test
    void DOCKING_섹션의_현재_수량보다_많은_수량을_차감하려고_하면_SectionCapacityUnderflowException이_발생한다() {
        Warehouse warehouse = new WarehouseTestBuilder().warehouseId(1L).capacity(10000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-DOCK-R-01").sectionType(SectionType.DOCKING)
                .temperatureType(TemperatureZone.ROOM).maxCapacity(3000).build();
        warehouse.receiveToDocking(Map.of(TemperatureZone.ROOM, 100));

        assertThatThrownBy(() -> warehouse.releaseFromDocking(Map.of(TemperatureZone.ROOM, 200)))
                .isInstanceOf(SectionCapacityUnderflowException.class);
    }
}