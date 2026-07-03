package com.kb.cosmetic_wms.oms.domain.service;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;

public final class HaversineDistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000d;

    private HaversineDistanceCalculator() {
    }

    public static long distanceMeters(GeoCoordinate origin, GeoCoordinate destination) {
        double lat1 = Math.toRadians(origin.latitude().doubleValue());
        double lat2 = Math.toRadians(destination.latitude().doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLng = Math.toRadians(
                destination.longitude().doubleValue() - origin.longitude().doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(EARTH_RADIUS_METERS * c);
    }
}
