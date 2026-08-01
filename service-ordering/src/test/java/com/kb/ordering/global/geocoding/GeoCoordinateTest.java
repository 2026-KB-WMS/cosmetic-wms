package com.kb.ordering.global.geocoding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeoCoordinateTest {

    @Test
    void 유효한_위도와_경도로_좌표를_생성할_수_있다() {
        GeoCoordinate coordinate = GeoCoordinate.of(37.4979, 127.0276);

        assertThat(coordinate.latitude()).isEqualByComparingTo(BigDecimal.valueOf(37.4979));
        assertThat(coordinate.longitude()).isEqualByComparingTo(BigDecimal.valueOf(127.0276));
    }

    @Test
    void 위도나_경도가_null이면_예외를_던진다() {
        assertThatThrownBy(() -> new GeoCoordinate(null, BigDecimal.valueOf(127.0276)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeoCoordinate(BigDecimal.valueOf(37.4979), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "-90.1, 127.0",
            "90.1, 127.0",
            "37.5, -180.1",
            "37.5, 180.1"
    })
    void 위도_경도_범위를_벗어나면_예외를_던진다(double latitude, double longitude) {
        assertThatThrownBy(() -> GeoCoordinate.of(latitude, longitude))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 위도_경도_경계값은_허용된다() {
        assertThat(GeoCoordinate.of(90, 180)).isNotNull();
        assertThat(GeoCoordinate.of(-90, -180)).isNotNull();
    }
}
