package com.kb.cosmetic_wms.domain.inbound.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InboundErrorCode implements ErrorCode {

    INBOUND_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "INBOUND_NOT_FOUND",
            "존재하지 않는 입고 전표입니다."
    ),
    INBOUND_ITEM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "INBOUND_ITEM_NOT_FOUND",
            "존재하지 않는 입고 품목입니다."
    ),
    INBOUND_PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "INBOUND_PRODUCT_NOT_FOUND",
            "입고 등록 대상 상품이 존재하지 않습니다."
    ),
    INBOUND_EMPTY_ITEMS(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "INBOUND_EMPTY_ITEMS",
            "입고 품목이 하나도 없어 작업을 시작할 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
