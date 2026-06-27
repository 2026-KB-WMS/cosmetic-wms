package com.kb.cosmetic_wms.storage;

import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.storage.domain.service.SectionCodeGenerator;
import com.kb.cosmetic_wms.storage.domain.exception.StorageErrorCode;
import com.kb.cosmetic_wms.storage.domain.exception.StorageValidationException;
import com.kb.cosmetic_wms.storage.domain.model.SectionCode;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SectionCodeGeneratorTest {

    @Test
    void 창고코드와_구역타입_온도타입_순번을_조합하여_올바른_섹션코드를_생성한다() {
        SectionCodeGenerator generator = new SectionCodeGenerator();

        SectionCode result = generator.generate("WH01", SectionType.HIGH_ROT, TemperatureZone.COOL, 3);

        assertThat(result.value()).isEqualTo("WH01-HIGH-C-03");
    }

    @Test
    void 순번이_두_자리수_이상이어도_포맷에_맞게_섹션코드를_생성한다() {
        SectionCodeGenerator generator = new SectionCodeGenerator();

        SectionCode result = generator.generate("WH01", SectionType.MID_ROT, TemperatureZone.ROOM, 12);

        assertThat(result.value()).isEqualTo("WH01-MID-R-12");
    }

    @Test
    void 순번이_0_이하이면_예외를_던진다() {
        SectionCodeGenerator generator = new SectionCodeGenerator();

        assertThatThrownBy(() ->
                generator.generate("WH01", SectionType.HIGH_ROT, TemperatureZone.ROOM, 0)
        )
                .isInstanceOf(StorageValidationException.class)
                .hasMessage(StorageErrorCode.INVALID_SECTION_SEQUENCE.getMessage());
    }
}