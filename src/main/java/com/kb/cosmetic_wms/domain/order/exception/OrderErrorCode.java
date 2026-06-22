package com.kb.cosmetic_wms.domain.order.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "ORDER_NOT_FOUND",
            "존재하지 않는 발주 전표입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}