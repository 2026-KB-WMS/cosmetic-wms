package com.kb.cosmetic_wms.domain.outbound.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OutboundErrorCode implements ErrorCode {

    OUTBOUND_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "OUTBOUND_NOT_FOUND",
            "존재하지 않는 출고 전표입니다."
    ),
    OUTBOUND_ORDER_ID_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "OUTBOUND_ORDER_ID_REQUIRED",
            "출고 전표 생성 시 발주 ID는 필수입니다."
    ),
    OUTBOUND_WAREHOUSE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "OUTBOUND_WAREHOUSE_REQUIRED",
            "출고 전표 생성 시 창고 ID는 필수입니다."
    ),
    OUTBOUND_TYPE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "OUTBOUND_TYPE_REQUIRED",
            "출고 전표 생성 시 출고 유형은 필수입니다."
    ),
    OUTBOUND_ALLOCATE_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "OUTBOUND_ALLOCATE_NOT_ALLOWED",
            "출고 대기(PENDING) 상태의 전표만 재고 할당이 가능합니다."
    ),
    OUTBOUND_PROCESSING_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "OUTBOUND_PROCESSING_NOT_ALLOWED",
            "재고 할당(ALLOCATED) 상태의 전표만 출고 작업을 시작할 수 있습니다."
    ),
    OUTBOUND_SHIP_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "OUTBOUND_SHIP_NOT_ALLOWED",
            "출고 준비 중(PROCESSING) 상태의 전표만 출하 완료 처리가 가능합니다."
    ),
    OUTBOUND_CANCEL_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "OUTBOUND_CANCEL_NOT_ALLOWED",
            "출고 대기(PENDING) 또는 재고 할당(ALLOCATED) 상태의 전표만 취소할 수 있습니다."
    ),
    OUTBOUND_INSUFFICIENT_STOCK(
            HttpStatus.CONFLICT,
            "OUTBOUND_INSUFFICIENT_STOCK",
            "발주 수량을 충족할 가용 재고가 부족합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}