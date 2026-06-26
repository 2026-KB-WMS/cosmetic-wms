package com.kb.cosmetic_wms.order.domain.exception;

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
    ),

    ORDER_STORE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "ORDER_STORE_REQUIRED",
            "발주 시 가맹점 정보는 필수입니다."
    ),
    ORDER_WAREHOUSE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "ORDER_WAREHOUSE_REQUIRED",
            "발주 시 창고 정보는 필수입니다."
    ),
    ORDER_ITEMS_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "ORDER_ITEMS_REQUIRED",
            "발주 항목은 최소 1개 이상이어야 합니다."
    ),
    ORDER_ITEM_PRODUCT_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "ORDER_ITEM_PRODUCT_REQUIRED",
            "발주 항목의 상품 정보는 필수입니다."
    ),
    ORDER_ITEM_QUANTITY_INVALID(
            HttpStatus.BAD_REQUEST,
            "ORDER_ITEM_QUANTITY_INVALID",
            "발주 수량은 0보다 커야 합니다."
    ),

    ORDER_CONFIRM_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "ORDER_CONFIRM_NOT_ALLOWED",
            "발주 신청(PENDING) 상태에서만 확정이 가능합니다."
    ),
    ORDER_PREPARATION_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "ORDER_PREPARATION_NOT_ALLOWED",
            "발주 확정(CONFIRMED) 상태에서만 배송 준비를 시작할 수 있습니다."
    ),
    ORDER_SHIP_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "ORDER_SHIP_NOT_ALLOWED",
            "배송 준비 중(PREPARING) 상태에서만 배송을 시작할 수 있습니다."
    ),
    ORDER_DELIVERY_COMPLETE_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "ORDER_DELIVERY_COMPLETE_NOT_ALLOWED",
            "배송 중(SHIPPED) 상태에서만 배송 완료 처리가 가능합니다."
    ),
    ORDER_CANCEL_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "ORDER_CANCEL_NOT_ALLOWED",
            "발주 신청(PENDING) 상태에서만 취소가 가능합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}