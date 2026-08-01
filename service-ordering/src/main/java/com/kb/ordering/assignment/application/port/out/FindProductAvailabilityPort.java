package com.kb.ordering.assignment.application.port.out;

import com.kb.ordering.assignment.application.port.out.dto.ProductAvailabilityView;

import java.util.Collection;
import java.util.List;

/**
 * 상품별 창고 가용 재고 조회 포트. 현재는 서비스 내부 직접 호출이나, inventory-view REST 클라이언트로 교체 예정.
 */
public interface FindProductAvailabilityPort {

    List<ProductAvailabilityView> findAvailabilityByProducts(Collection<Long> productIds);
}
