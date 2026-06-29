package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InboundErrorCode implements ErrorCode {

    // 조회 실패
    INBOUND_NOT_FOUND(HttpStatus.NOT_FOUND, "INBOUND_NOT_FOUND", "존재하지 않는 입고 전표입니다."),
    INBOUND_LINE_NOT_FOUND(HttpStatus.NOT_FOUND, "INBOUND_LINE_NOT_FOUND", "존재하지 않는 입고 품목 라인입니다."),
    INBOUND_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "INBOUND_PRODUCT_NOT_FOUND", "입고 등록 대상 상품이 존재하지 않습니다."),
    INBOUND_WAREHOUSE_NOT_FOUND(HttpStatus.NOT_FOUND, "INBOUND_WAREHOUSE_NOT_FOUND", "입고 처리에 필요한 창고가 존재하지 않습니다."),
    INBOUND_PARTNER_NOT_FOUND(HttpStatus.NOT_FOUND, "INBOUND_PARTNER_NOT_FOUND", "입고 처리에 필요한 협력사가 존재하지 않습니다."),

    // 입력값 검증 실패
    INBOUND_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "INBOUND_DATE_REQUIRED", "입고 예정일은 필수입니다."),
    INBOUND_PAST_DATE(HttpStatus.BAD_REQUEST, "INBOUND_PAST_DATE", "입고 예정일은 현재 날짜보다 과거일 수 없습니다."),
    INBOUND_WAREHOUSE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "INBOUND_WAREHOUSE_ID_REQUIRED", "입고 창고 정보는 필수입니다."),
    INBOUND_PARTNER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "INBOUND_PARTNER_ID_REQUIRED", "입고 파트너 정보는 필수입니다."),
    INBOUND_ITEM_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "INBOUND_ITEM_INVALID_QUANTITY", "입고 예정 수량은 0보다 커야 합니다."),
    INBOUND_ITEM_PRODUCT_REQUIRED(HttpStatus.BAD_REQUEST, "INBOUND_ITEM_PRODUCT_REQUIRED", "입고 상품 정보는 필수입니다."),
    INBOUND_LINES_REQUIRED(HttpStatus.BAD_REQUEST, "INBOUND_LINES_REQUIRED", "입고 품목 라인이 최소 하나 이상 필요합니다."),
    INBOUND_RECEIVE_LINE_MISMATCH(HttpStatus.BAD_REQUEST, "INBOUND_RECEIVE_LINE_MISMATCH", "수령 확인 요청의 라인 목록이 입고 전표의 품목 라인과 일치하지 않습니다."),

    // 수용 용량 초과
    INBOUND_WAREHOUSE_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "INBOUND_WAREHOUSE_CAPACITY_EXCEEDED", "창고의 DOCKING 구역 수용 용량이 부족하여 입고를 처리할 수 없습니다."),

    // 상태 전이 불가
    INBOUND_INVALID_RECEIVE_STATUS(HttpStatus.CONFLICT, "INBOUND_INVALID_RECEIVE_STATUS", "입고 예정(SCHEDULED) 상태에서만 수령 확인이 가능합니다. (현재 상태: %s)"),
    INBOUND_INVALID_CANCEL_STATUS(HttpStatus.CONFLICT, "INBOUND_INVALID_CANCEL_STATUS", "이미 작업이 진행되었거나 완료된 입고 건은 취소할 수 없습니다. (현재 상태: %s)");

    private final HttpStatus status;
    private final String code;
    private final String message;
}