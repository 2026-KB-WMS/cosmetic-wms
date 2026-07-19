package com.kb.ordering.assignment.application.port.out;

import com.kb.ordering.assignment.domain.model.WarehouseCandidate;

import java.util.Collection;
import java.util.List;

public interface LoadWarehouseCandidatesPort {

    /**
     * 전체 창고를 후보로 로드하고, 요청 상품들의 가용 재고 요약(수량·최근접 유통기한)을 함께 채워 반환한다.
     */
    List<WarehouseCandidate> loadCandidates(Collection<Long> productIds);
}
