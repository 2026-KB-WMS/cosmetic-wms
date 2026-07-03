package com.kb.cosmetic_wms.oms.domain.service;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;

/**
 * 주행거리 산정 함수. 도메인이 application 포트에 의존하지 않도록
 * 호출 측(application service)이 RoutingPort를 이 인터페이스로 감싸 주입한다.
 */
@FunctionalInterface
public interface DrivingDistanceProvider {

    long drivingDistanceMeters(GeoCoordinate origin, GeoCoordinate destination);
}
