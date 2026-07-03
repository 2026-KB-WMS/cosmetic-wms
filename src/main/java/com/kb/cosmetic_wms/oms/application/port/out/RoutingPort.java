package com.kb.cosmetic_wms.oms.application.port.out;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;

public interface RoutingPort {

    /**
     * 외부 Routing API를 통해 두 좌표 간 실제 주행거리(m)를 반환한다.
     * 호출 실패 시 구현체가 예외를 던지며, 폴백 전략(하버사인 대체)은 호출 측 정책에 따른다.
     */
    long drivingDistanceMeters(GeoCoordinate origin, GeoCoordinate destination);
}
