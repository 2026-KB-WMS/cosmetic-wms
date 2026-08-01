package com.kb.ordering.assignment;

import com.kb.ordering.assignment.domain.service.HaversineDistanceCalculator;
import com.kb.ordering.global.geocoding.GeoCoordinate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HaversineDistanceCalculatorTest {

    private static final GeoCoordinate SEOUL_CITY_HALL = GeoCoordinate.of(37.5665, 126.9780);
    private static final GeoCoordinate BUSAN_CITY_HALL = GeoCoordinate.of(35.1798, 129.0750);

    @Test
    void 서울_부산_직선거리는_약_325km이다() {
        long distance = HaversineDistanceCalculator.distanceMeters(SEOUL_CITY_HALL, BUSAN_CITY_HALL);

        assertThat(distance).isBetween(320_000L, 330_000L);
    }

    @Test
    void 동일_좌표_간_거리는_0이다() {
        assertThat(HaversineDistanceCalculator.distanceMeters(SEOUL_CITY_HALL, SEOUL_CITY_HALL)).isZero();
    }

    @Test
    void 출발지와_목적지를_바꿔도_거리는_동일하다() {
        long forward = HaversineDistanceCalculator.distanceMeters(SEOUL_CITY_HALL, BUSAN_CITY_HALL);
        long backward = HaversineDistanceCalculator.distanceMeters(BUSAN_CITY_HALL, SEOUL_CITY_HALL);

        assertThat(forward).isEqualTo(backward);
    }
}
