package com.kb.ordering.global.geocoding;

public interface GeocodingPort {

    GeoCoordinate geocode(String address);
}
