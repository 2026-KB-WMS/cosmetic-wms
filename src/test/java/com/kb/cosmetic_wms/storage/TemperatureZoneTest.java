package com.kb.cosmetic_wms.storage;

import com.kb.cosmetic_wms.storage.domain.exception.StorageErrorCode;
import com.kb.cosmetic_wms.storage.domain.exception.StorageValidationException;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemperatureZoneTest {

    @Test
    void 구분_코드로_온도_구역을_찾는다() {
        assertThat(TemperatureZone.fromShortCode("R")).isEqualTo(TemperatureZone.ROOM);
        assertThat(TemperatureZone.fromShortCode("C")).isEqualTo(TemperatureZone.COOL);
    }

    @Test
    void 알_수_없는_구분_코드이면_예외를_던진다() {
        assertThatThrownBy(() -> TemperatureZone.fromShortCode("X"))
                .isInstanceOf(StorageValidationException.class)
                .extracting("errorCode")
                .isEqualTo(StorageErrorCode.INVALID_TEMPERATURE_CODE);
    }
}
