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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

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

    @ParameterizedTest(name = "{0} 구역에 품질:{1}, 할당:{2} 조합은 예외가 발생한다")
    @MethodSource("provideInvalidSectionCombinations")
    void 규칙에_위배되는_구역_타입과_상태_조합이면_예외를_던진다(
            SectionType type, SectionQualityStatus quality, SectionAllocationStatus allocation
    ) {
        assertThatThrownBy(() ->
                new SectionTestBuilder()
                        .sectionType(type)
                        .qualityStatus(quality)
                        .allocationStatus(allocation)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.INVALID_SECTION_STATE_COMBINATION_MESSAGE);
    }

    @Test
    void 격리_구역의_온도_타입이_COOL이면_예외를_던진다() {
        assertThatThrownBy(() ->
                new SectionTestBuilder()
                        .sectionType(SectionType.QUARANTINE)
                        .qualityStatus(SectionQualityStatus.HOLD)
                        .allocationStatus(SectionAllocationStatus.EXCLUDED)
                        .temperatureType(TemperatureType.COOL)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(StorageConstants.QUARANTINE_MUST_BE_ROOM_MESSAGE);
    }

    private static Stream<Arguments> provideInvalidSectionCombinations() {
        return Stream.of(
                // 1. DOCKING 구역인데 품질이 NORMAL이거나, 할당이 AVAILABLE/EXCLUDED인 경우
                Arguments.of(SectionType.DOCKING, SectionQualityStatus.NORMAL, SectionAllocationStatus.NONE),
                Arguments.of(SectionType.DOCKING, SectionQualityStatus.INSPECTING, SectionAllocationStatus.AVAILABLE),
                Arguments.of(SectionType.DOCKING, SectionQualityStatus.INSPECTING, SectionAllocationStatus.EXCLUDED),

                // 2. QUARANTINE(격리) 구역인데 품질이 NORMAL이거나, 할당이 AVAILABLE/NONE인 경우
                Arguments.of(SectionType.QUARANTINE, SectionQualityStatus.NORMAL, SectionAllocationStatus.EXCLUDED),
                Arguments.of(SectionType.QUARANTINE, SectionQualityStatus.HOLD, SectionAllocationStatus.AVAILABLE),
                Arguments.of(SectionType.QUARANTINE, SectionQualityStatus.HOLD, SectionAllocationStatus.NONE),

                // 3. 보관 구역(HIGH, MID, LOW)인데 품질이 INSPECTING이거나 할당이 NONE인 경우
                Arguments.of(SectionType.HIGH_ROT, SectionQualityStatus.INSPECTING, SectionAllocationStatus.AVAILABLE),
                Arguments.of(SectionType.MID_ROT, SectionQualityStatus.NORMAL, SectionAllocationStatus.NONE)
        );
    }
}
