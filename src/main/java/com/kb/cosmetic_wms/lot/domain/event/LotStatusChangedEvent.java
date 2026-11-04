package com.kb.cosmetic_wms.lot.domain.event;

/**
 * 로트 상태가 바뀌었음을 알린다. 구독하는 쪽이 lot의 도메인 타입을 알 필요가 없도록 상태는 이름 문자열로 싣는다.
 *
 * @param allocatable 이 상태의 로트가 출고 대상이 될 수 있는가
 */
public record LotStatusChangedEvent(Long lotId, String status, boolean allocatable) {
}
