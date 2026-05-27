package com.kb.cosmetic_wms.domain.storage;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.constants.StorageConstants;
import com.kb.cosmetic_wms.domain.storage.entity.Section;
import com.kb.cosmetic_wms.domain.storage.enums.SectionAllocationStatus;
import com.kb.cosmetic_wms.domain.storage.enums.SectionQualityStatus;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;
import com.kb.cosmetic_wms.domain.storage.fixture.SectionTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SectionEntityTest {

    @Test
    void 섹션의_최대_수용_가능_수량이_0_이하이면_예외를_던진다() {
        assertThatThrownBy(() ->
                new SectionTestBuilder()
                        .maxCapacity(StorageConstants.MIN_CAPACITY_BOUND)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.INVALID_SECTION_MAX_CAPACITY_MESSAGE);
    }

    @Test
    void 섹션에_수량을_추가했을_때_최대_수용량을_초과하면_예외를_던진다() {
        // given
        Section section = new SectionTestBuilder().maxCapacity(1000).build();
        section.plusCapacity(800);

        // when & then
        assertThatThrownBy(() -> section.plusCapacity(300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.SECTION_CAPACITY_OVERFLOW_MESSAGE);
    }

    @Test
    void 섹션의_수량을_차감했을_때_현재_수량이_음수가_되면_예외를_던진다() {
        Section section = new SectionTestBuilder().maxCapacity(1000).build();
        section.plusCapacity(100);

        assertThatThrownBy(() -> section.minusCapacity(101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.SECTION_CAPACITY_UNDERFLOW_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 섹션_코드가_null이거나_공백이면_예외를_던진다(String invalidCode) {
        assertThatThrownBy(() ->
                new SectionTestBuilder().sectionCode(invalidCode).build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.SECTION_CODE_REQUIRED_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"WH01-HIGH-R", "WH01-HIGH-01", "HIGH-R-01", "창고01-HIGH-R-01", "WH01-INVALID-R-01"})
    void 섹션_코드_포맷이_정해진_규격과_다를_경우_예외를_던진다(String invalidCode) {
        assertThatThrownBy(() ->
                new SectionTestBuilder()
                        .sectionCode(invalidCode)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.INVALID_SECTION_CODE_PATTERN_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 섹션_이름이_null이거나_공백이면_예외를_던진다(String invalidSectionName) {
        assertThatThrownBy(() ->
                new SectionTestBuilder().sectionName(invalidSectionName).build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.SECTION_NAME_REQUIRED_MESSAGE);
    }

    @ParameterizedTest(name = "{0} 구역을 생성하면 품질은 NORMAL, 할당은 AVAILABLE로 자동 세팅된다")
    @EnumSource(value = SectionType.class, names = {"HIGH_ROT", "MID_ROT", "LOW_ROT"})
    void 보관_구역을_생성하면_품질은_NORMAL_할당은_AVAILABLE로_자동_세팅된다(SectionType sectionType) {
        // given & when
        Section section = new SectionTestBuilder()
                .sectionType(sectionType)
                .build();

        assertThat(section.getSectionType()).isEqualTo(sectionType);
        assertThat(section.getQualityStatus()).isEqualTo(SectionQualityStatus.NORMAL);
        assertThat(section.getAllocationStatus()).isEqualTo(SectionAllocationStatus.AVAILABLE);
    }

    @Test
    void 검수_대기_구역을_생성하면_품질은_INSPECTING_할당은_NONE으로_자동_세팅된다() {
        // given & when
        Section section = new SectionTestBuilder()
                .sectionType(SectionType.DOCKING)
                .build();

        // then
        assertThat(section.getQualityStatus()).isEqualTo(SectionQualityStatus.INSPECTING);
        assertThat(section.getAllocationStatus()).isEqualTo(SectionAllocationStatus.NONE);
    }

    @Test
    void 격리_폐기_구역을_생성하면_품질은_HOLD_할당은_EXCLUDED_온도는_ROOM으로_자동_세팅된다() {
        // given & when
        Section section = new SectionTestBuilder()
                .sectionType(SectionType.QUARANTINE)
                .build();

        // then
        assertThat(section.getQualityStatus()).isEqualTo(SectionQualityStatus.HOLD);
        assertThat(section.getAllocationStatus()).isEqualTo(SectionAllocationStatus.EXCLUDED);
        assertThat(section.getTemperatureType()).isEqualTo(TemperatureType.ROOM);
    }
}
