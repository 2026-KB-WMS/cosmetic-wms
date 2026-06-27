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
    INBOUND_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "INBOUND_ITEM_NOT_FOUND", "존재하지 않는 입고 품목입니다."),
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
    INBOUND_MANUFACTURE_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "INBOUND_MANUFACTURE_DATE_REQUIRED", "제조일자는 필수입니다."),
    INBOUND_EXPIRATION_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "INBOUND_EXPIRATION_DATE_REQUIRED", "유통기한은 필수입니다."),
    INBOUND_EXPIRATION_BEFORE_MANUFACTURE(HttpStatus.BAD_REQUEST, "INBOUND_EXPIRATION_BEFORE_MANUFACTURE", "유통기한은 제조일자 이후여야 합니다."),
    INBOUND_PUTAWAY_LOT_REQUIRED(HttpStatus.BAD_REQUEST, "INBOUND_PUTAWAY_LOT_REQUIRED", "적재 시 생성된 로트(Lot) 정보는 필수입니다."),
    INBOUND_PUTAWAY_SECTION_REQUIRED(HttpStatus.BAD_REQUEST, "INBOUND_PUTAWAY_SECTION_REQUIRED", "적재될 섹션 정보는 필수입니다."),

    // 상태 전이 불가
    INBOUND_EMPTY_ITEMS(HttpStatus.UNPROCESSABLE_ENTITY, "INBOUND_EMPTY_ITEMS", "입고 품목이 하나도 없어 작업을 시작할 수 없습니다."),
    INBOUND_INSPECTION_INCOMPLETE(HttpStatus.UNPROCESSABLE_ENTITY, "INBOUND_INSPECTION_INCOMPLETE", "아직 적재가 완료되지 않았거나 검수 중인 품목이 존재하여 입고 완료 처리가 불가능합니다."),
    INBOUND_INVALID_ADD_ITEM_STATUS(HttpStatus.CONFLICT, "INBOUND_INVALID_ADD_ITEM_STATUS", "입고 예정(SCHEDULED) 상태일 때만 품목을 추가할 수 있습니다."),
    INBOUND_INVALID_START_STATUS(HttpStatus.CONFLICT, "INBOUND_INVALID_START_STATUS", "입고 예정 상태에서만 작업을 시작할 수 있습니다. (현재 상태: %s)"),
    INBOUND_INVALID_COMPLETE_STATUS(HttpStatus.CONFLICT, "INBOUND_INVALID_COMPLETE_STATUS", "작업이 진행 중인 상태에서만 입고 완료 처리가 가능합니다. (현재 상태: %s)"),
    INBOUND_INVALID_CANCEL_STATUS(HttpStatus.CONFLICT, "INBOUND_INVALID_CANCEL_STATUS", "이미 작업이 진행되었거나 완료된 입고 건은 취소할 수 없습니다. (현재 상태: %s)"),
    INBOUND_INVALID_PUTAWAY_STATUS(HttpStatus.CONFLICT, "INBOUND_INVALID_PUTAWAY_STATUS", "이미 적재가 완료되었거나 검수가 진행된 품목입니다. (현재 상태: %s)"),
    INBOUND_INVALID_APPROVE_STATUS(HttpStatus.CONFLICT, "INBOUND_INVALID_APPROVE_STATUS", "검수 중 상태에서만 정상 완료 처리가 가능합니다."),
    INBOUND_INVALID_HOLD_STATUS(HttpStatus.CONFLICT, "INBOUND_INVALID_HOLD_STATUS", "검수 중 상태에서만 검수 보류 처리가 가능합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
