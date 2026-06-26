package com.kb.cosmetic_wms.outbound.domain.exception;

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
    ),
    OUTBOUND_ITEM_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "OUTBOUND_ITEM_REQUIRED",
            "출고 전표에는 최소 1개 이상의 품목이 필요합니다."
    ),
    OUTBOUND_INCOMPLETE_PICKING(
            HttpStatus.CONFLICT,
            "OUTBOUND_INCOMPLETE_PICKING",
            "모든 출고 품목의 피킹이 완료되지 않아 출고 확정을 할 수 없습니다."
    ),
    OUTBOUND_ITEM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "OUTBOUND_ITEM_NOT_FOUND",
            "해당 재고 ID의 출고 품목이 없습니다."
    ),
    OUTBOUND_PICKING_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "OUTBOUND_PICKING_NOT_ALLOWED",
            "피킹 수량 변경은 출고 준비 중(PROCESSING) 상태에서만 가능합니다."
    ),
    OUTBOUND_INVALID_TARGET_QUANTITY(
            HttpStatus.BAD_REQUEST,
            "OUTBOUND_INVALID_TARGET_QUANTITY",
            "출고 지시 수량은 0 이하일 수 없습니다."
    ),
    OUTBOUND_INVALID_PICKED_QUANTITY(
            HttpStatus.BAD_REQUEST,
            "OUTBOUND_INVALID_PICKED_QUANTITY",
            "실제 피킹 수량은 음수일 수 없습니다."
    ),
    OUTBOUND_EXCEED_PICKED_QUANTITY(
            HttpStatus.BAD_REQUEST,
            "OUTBOUND_EXCEED_PICKED_QUANTITY",
            "실제 피킹 수량은 출고 지시 수량을 초과할 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}