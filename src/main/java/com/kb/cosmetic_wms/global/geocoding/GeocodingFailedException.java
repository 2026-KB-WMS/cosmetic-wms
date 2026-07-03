package com.kb.cosmetic_wms.global.geocoding;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class GeocodingFailedException extends BusinessException {

    public GeocodingFailedException(GeocodingErrorCode errorCode) {
        super(errorCode);
    }
}
