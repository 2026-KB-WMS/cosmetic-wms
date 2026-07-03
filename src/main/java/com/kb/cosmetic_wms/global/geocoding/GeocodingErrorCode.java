package com.kb.cosmetic_wms.global.geocoding;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GeocodingErrorCode implements ErrorCode {

    GEOCODING_ADDRESS_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "GEOCODING_ADDRESS_NOT_FOUND",
            "주소에 해당하는 좌표를 찾을 수 없습니다."
    ),
    GEOCODING_API_ERROR(
            HttpStatus.BAD_GATEWAY,
            "GEOCODING_API_ERROR",
            "지오코딩 API 호출에 실패했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
