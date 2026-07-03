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
    INVALID_WAREHOUSE_CANDIDATE(
            HttpStatus.BAD_REQUEST,
            "INVALID_WAREHOUSE_CANDIDATE",
            "창고 후보는 창고 ID와 좌표가 필수입니다."
    ),
    INVALID_PRODUCT_STOCK(
            HttpStatus.BAD_REQUEST,
            "INVALID_PRODUCT_STOCK",
            "재고 요약은 0 이상의 가용 수량과 (재고 보유 시) 최근접 유통기한이 필수입니다."
    ),
    INVALID_DEMAND(
            HttpStatus.BAD_REQUEST,
            "INVALID_DEMAND",
            "배정 요청 품목은 상품 ID와 1 이상의 수량이 필수입니다."
    ),
    INVALID_ASSIGNMENT_INPUT(
            HttpStatus.BAD_REQUEST,
            "INVALID_ASSIGNMENT_INPUT",
            "창고 배정에는 배송지 좌표·기준일·주행거리 산정 함수와 1개 이상의 품목이 필요합니다."
    ),
    INVALID_ASSIGNMENT_POLICY(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INVALID_ASSIGNMENT_POLICY",
            "창고 배정 정책 설정이 유효하지 않습니다. (후보 수 > 0, 가중치 ≥ 0, 가중치 합 > 0)"
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
