package com.kb.ordering.product;

import com.kb.ordering.product.domain.product.exception.InvalidVolumeException;
import com.kb.ordering.product.domain.product.exception.InvalidVolumeUnitException;
import com.kb.ordering.product.domain.product.valueobject.Volume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VolumeTest {

    @Test
    void 유효한_값이면_Volume_객체가_정상_생성된다() {
        Volume volume = Volume.of(150, "ml");

        assertThat(volume.value()).isEqualTo(150);
        assertThat(volume.unit()).isEqualTo("ml");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10, -500})
    void 용량_수치가_0_이하이면_예외를_던진다(int invalidValue) {
        assertThatThrownBy(() -> Volume.of(invalidValue, "ml"))
                .isInstanceOf(InvalidVolumeException.class)
                .hasMessage("화장품 용량은 " + Volume.MIN_VALUE + "보다 커야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {10001, 25000})
    void 화장품_용량이_최대_제한을_초과하면_예외를_던진다(int exceedValue) {
        assertThatThrownBy(() -> Volume.of(exceedValue, "ml"))
                .isInstanceOf(InvalidVolumeException.class)
                .hasMessage("올바르지 않은 대용량 수치입니다. (최대 " + Volume.MAX_VALUE + "까지 허용)");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 용량_단위가_비어있거나_공백이면_예외를_던진다(String invalidUnit) {
        assertThatThrownBy(() -> Volume.of(150, invalidUnit))
                .isInstanceOf(InvalidVolumeUnitException.class)
                .hasMessage("용량 단위는 필수 입력 항목입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"l", "L", "kg", "밀리리터"})
    void 허용되지_않은_단위가_입력되면_예외를_던진다(String invalidUnit) {
        assertThatThrownBy(() -> Volume.of(150, invalidUnit))
                .isInstanceOf(InvalidVolumeUnitException.class)
                .hasMessage("올바르지 않은 용량 단위입니다. (ml, g, ea, oz, fl.oz 허용)");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ML", "mL", "Ml", "G", "EA", "Ea", "fl. oz", "FL. OZ", "Fl. Oz"})
    void 대소문자와_공백이_혼용되어_입력되어도_소문자로_정상_생성된다(String mixedCaseUnit) {
        Volume volume = Volume.of(150, mixedCaseUnit);

        String expected = mixedCaseUnit.toLowerCase().replace(" ", "");
        assertThat(volume.unit()).isEqualTo(expected);
    }
}