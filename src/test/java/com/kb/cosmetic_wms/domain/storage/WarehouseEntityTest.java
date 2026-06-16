package com.kb.cosmetic_wms.domain.storage;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.constants.StorageConstants;
import com.kb.cosmetic_wms.domain.storage.entity.Section;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;
import com.kb.cosmetic_wms.domain.storage.exception.StorageErrorCode;
import com.kb.cosmetic_wms.domain.storage.exception.StorageExceedCapacityException;
import com.kb.cosmetic_wms.domain.storage.fixture.SectionTestBuilder;
import com.kb.cosmetic_wms.domain.storage.fixture.WarehouseTestBuilder;
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
        // given & when
        Warehouse warehouse = new WarehouseTestBuilder().build();

        // then
        assertThat(warehouse.getWarehouseName()).isEqualTo("인천 제1 센터");
        assertThat(warehouse.getAddress()).isEqualTo("인천광역시 중구");
        assertThat(warehouse.getCapacity()).isEqualTo(10000);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 창고_이름이_null이거나_공백이면_예외를_던진다(String invalidWarehouseName) {
        assertThatThrownBy(() ->
                new WarehouseTestBuilder()
                        .warehouseName(invalidWarehouseName)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.WAREHOUSE_NAME_REQUIRED_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 창고_주소가_null이거나_공백이면_예외를_던진다(String invalidAddress) {
        assertThatThrownBy(() ->
                new WarehouseTestBuilder()
                        .address(invalidAddress)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.ADDRESS_REQUIRED_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 창고_적정_온도가_null이거나_공백이면_예외를_던진다(String invalidTemp) {
        assertThatThrownBy(() ->
                new WarehouseTestBuilder()
                        .targetTemp(invalidTemp)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.TARGET_TEMP_REQUIRED_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"10~25", "10-25도", "상온", "abcdefg"})
    void 적정_온도가_정해진_포맷과_다를_경우_예외를_던진다(String invalidTemp) {
        assertThatThrownBy(() ->
                new WarehouseTestBuilder()
                        .targetTemp(invalidTemp)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.INVALID_TARGET_TEMP_PATTERN_MESSAGE);
    }

    @Test
    void 창고_수용_한도가_0_이하이면_예외를_던진다() {
        Assertions.assertThatThrownBy(() ->
                        new WarehouseTestBuilder()
                                .capacity(StorageConstants.MIN_CAPACITY_BOUND)
                                .build()
                )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.INVALID_CAPACITY_MESSAGE);
    }

    @Test
    void 추가하려는_섹션들의_최대_수용량_합이_창고_전체_수용_한도를_초과하면_예외를_던진다() {
        // given
        Warehouse warehouse = new WarehouseTestBuilder().capacity(10000).build();

        new SectionTestBuilder()
                .warehouse(warehouse)
                .sectionCode("WH01-MID-R-01")
                .maxCapacity(6000)
                .build();

        // when & then
        assertThatThrownBy(() ->
                new SectionTestBuilder()
                        .warehouse(warehouse)
                        .sectionCode("WH01-HIGH-R-01")
                        .maxCapacity(6000)
                        .build()
        )
                .isInstanceOf(StorageExceedCapacityException.class)
                .hasMessage(StorageErrorCode.EXCEED_WAREHOUSE_CAPACITY.getMessage());
    }

    @Test
    void 창고_내에_동일한_코드를_가진_섹션이_이미_존재하면_예외를_던진다() {
        // given
        Warehouse warehouse = new WarehouseTestBuilder().build();

        // when & then
        new SectionTestBuilder()
                .warehouse(warehouse)
                .sectionCode("WH01-HIGH-R-01")
                .build();

        assertThatThrownBy(() ->
                new SectionTestBuilder()
                        .warehouse(warehouse)
                        .sectionCode("WH01-HIGH-R-01")
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.DUPLICATE_SECTION_CODE_MESSAGE);
    }

    @Test
    void 규격에_맞는_보관_구역들을_추가하면_창고에_정상_등록된다() {
        // given
        Warehouse warehouse = new WarehouseTestBuilder().capacity(10000).build();

        // when
        Section dockingSection = new SectionTestBuilder()
                .warehouse(warehouse)
                .sectionCode("WH01-DOCK-R-01")
                .sectionType(SectionType.DOCKING)
                .maxCapacity(2000)
                .build();

        Section highRotSection = new SectionTestBuilder()
                .warehouse(warehouse)
                .sectionCode("WH01-HIGH-C-01")
                .sectionType(SectionType.HIGH_ROT)
                .temperatureType(TemperatureType.COOL)
                .maxCapacity(5000)
                .build();

        // then
        assertThat(warehouse.getSections()).hasSize(2);
        assertThat(dockingSection.getWarehouse()).isEqualTo(warehouse);
        assertThat(highRotSection.getWarehouse()).isEqualTo(warehouse);
    }

    @Test
    void 추가하려는_섹션들의_용량_합이_창고_전체_한도와_일치하면_예외_없이_정상_등록된다() {
        // given
        Warehouse warehouse = new WarehouseTestBuilder().capacity(10000).build();
        new SectionTestBuilder()
                .warehouse(warehouse)
                .sectionCode("WH01-HIGH-R-01")
                .maxCapacity(7000)
                .build();

        // when & then
        assertDoesNotThrow(() ->
                new SectionTestBuilder()
                        .warehouse(warehouse)
                        .sectionCode("WH01-LOW-R-01")
                        .maxCapacity(3000)
                        .build()
        );

        assertThat(warehouse.getSections()).hasSize(2);
    }
}
