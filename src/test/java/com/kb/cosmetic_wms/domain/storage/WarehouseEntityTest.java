package com.kb.cosmetic_wms.domain.storage;

import com.kb.cosmetic_wms.domain.storage.constants.StorageConstants;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.fixture.WarehouseTestBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
