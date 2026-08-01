package com.kb.ordering.global.geocoding;

import com.kb.common.error.BusinessException;

public class GeocodingFailedException extends BusinessException {

    public GeocodingFailedException(GeocodingErrorCode errorCode) {
        super(errorCode);
    }
}
