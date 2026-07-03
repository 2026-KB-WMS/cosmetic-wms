package com.kb.cosmetic_wms.oms.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OmsErrorCode implements ErrorCode {

    NO_ASSIGNABLE_WAREHOUSE(
            HttpStatus.CONFLICT,
            "NO_ASSIGNABLE_WAREHOUSE",
            "발주 품목 전량을 충족할 수 있는 창고가 없습니다."
    ),
    ROUTING_API_ERROR(
            HttpStatus.BAD_GATEWAY,
            "ROUTING_API_ERROR",
            "주행거리 산정 API 호출에 실패했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
