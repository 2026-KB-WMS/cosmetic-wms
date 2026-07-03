package com.kb.cosmetic_wms.oms.application.port.out;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;

public interface LoadStoreLocationPort {

    GeoCoordinate loadStoreLocation(Long storeId);
}
