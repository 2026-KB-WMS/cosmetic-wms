package com.kb.cosmetic_wms.domain.storage;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.constants.StorageConstants;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SectionCodeGeneratorTest {

    @Test
    void 창고코드와_구역타입_온도타입_순번을_조합하여_올바른_섹션코드를_생성한다() {
        // given
        SectionCodeGenerator generator = new SectionCodeGenerator();

        // when
        String result = generator.generate(
                "WH01", SectionType.HIGH_ROT, TemperatureType.COOL, 3);

        // then
        assertThat(result).isEqualTo("WH01-HIGH-C-03");
    }

    @Test
    void 순번이_두_자리수_이상이어도_포맷에_맞게_섹션코드를_생성한다() {
        // given
        SectionCodeGenerator generator = new SectionCodeGenerator();

        // when
        String result = generator.generate(
                "WH01", SectionType.MID_ROT, TemperatureType.ROOM, 12);

        // then
        assertThat(result).isEqualTo("WH01-MID-R-12");
    }

    @Test
    void 순번이_0_이하이면_예외를_던진다() {
        // given
        SectionCodeGenerator generator = new SectionCodeGenerator();

        // when & then
        assertThatThrownBy(() ->
                generator.generate(
                        "WH01", SectionType.HIGH_ROT,
                        TemperatureType.ROOM, 0)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.INVALID_SECTION_SEQUENCE_MESSAGE);
    }
}
