package com.kb.ordering.global.geocoding;

import com.kb.common.error.ErrorCode;
import com.kb.common.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeocodingErrorCode implements ErrorCode {

    GEOCODING_ADDRESS_NOT_FOUND(
            ErrorType.INVALID_REQUEST,
            "GEOCODING_ADDRESS_NOT_FOUND",
            "주소에 해당하는 좌표를 찾을 수 없습니다."
    ),
    GEOCODING_API_ERROR(
            ErrorType.EXTERNAL_SERVICE_ERROR,
            "GEOCODING_API_ERROR",
            "지오코딩 API 호출에 실패했습니다."
    );

    private final ErrorType type;
    private final String code;
    private final String message;
}
