package com.kb.cosmetic_wms.storage.application.port.in;

/**
 * 입고 예정 상품 수량을 온도대별로 환산해 창고 도킹 구역이 수용 가능한지 검사한다.
 */
public interface CheckWarehouseCapacityUseCase {

    boolean canAccommodate(CheckWarehouseCapacityCommand command);
}
