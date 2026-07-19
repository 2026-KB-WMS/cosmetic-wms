package com.kb.ordering.assignment.application.port.out;

import com.kb.ordering.global.geocoding.GeoCoordinate;

public interface LoadStoreLocationPort {

    GeoCoordinate loadStoreLocation(Long storeId);
}
