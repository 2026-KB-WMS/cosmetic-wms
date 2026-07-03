package com.kb.cosmetic_wms.global.geocoding;

import java.math.BigDecimal;

public record GeoCoordinate(BigDecimal latitude, BigDecimal longitude) {

    private static final BigDecimal MIN_LATITUDE = BigDecimal.valueOf(-90);
    private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
    private static final BigDecimal MIN_LONGITUDE = BigDecimal.valueOf(-180);
    private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);

    public GeoCoordinate {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("위도/경도는 필수입니다.");
        }
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new IllegalArgumentException("위도는 -90 ~ 90 범위여야 합니다: " + latitude);
        }
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new IllegalArgumentException("경도는 -180 ~ 180 범위여야 합니다: " + longitude);
        }
    }

    public static GeoCoordinate of(double latitude, double longitude) {
        return new GeoCoordinate(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    }
}
