package com.kb.cosmetic_wms.global.geocoding;

public interface GeocodingPort {

    GeoCoordinate geocode(String address);
}
