package com.kb.ordering.assignment.application.port.out;

import com.kb.ordering.assignment.application.port.out.dto.WarehouseView;

import java.util.List;

/**
 * 창고 목록 조회 포트. 현재는 서비스 내부 직접 호출이나, WMS REST 클라이언트로 교체 예정.
 */
public interface FindWarehousePort {

    List<WarehouseView> findAll();
}
