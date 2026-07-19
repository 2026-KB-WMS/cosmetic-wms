package com.kb.ordering.assignment.domain.service;

import com.kb.ordering.global.geocoding.GeoCoordinate;

@FunctionalInterface
public interface DrivingDistanceProvider {

    long drivingDistanceMeters(GeoCoordinate origin, GeoCoordinate destination);
}
