package com.kb.cosmetic_wms.lot;

import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class LotStatusTest {

    @Test
    void 정상_상태의_로트만_재고를_출고할_수_있다() {
        assertThat(LotStatus.AVAILABLE.isAllocatable()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = LotStatus.class, names = "AVAILABLE", mode = EnumSource.Mode.EXCLUDE)
    void 정상이_아닌_로트의_재고는_출고할_수_없다(LotStatus status) {
        assertThat(status.isAllocatable()).isFalse();
    }
}
