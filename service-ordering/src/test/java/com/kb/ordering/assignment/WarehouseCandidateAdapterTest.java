package com.kb.ordering.assignment;

import com.kb.ordering.assignment.adapter.out.external.WarehouseCandidateAdapter;
import com.kb.ordering.assignment.application.port.out.FindProductAvailabilityPort;
import com.kb.ordering.assignment.application.port.out.FindWarehousePort;
import com.kb.ordering.assignment.application.port.out.dto.ProductAvailabilityView;
import com.kb.ordering.assignment.application.port.out.dto.WarehouseView;
import com.kb.ordering.assignment.domain.model.WarehouseCandidate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WarehouseCandidateAdapterTest {

    @Mock
    private FindWarehousePort findWarehousePort;

    @Mock
    private FindProductAvailabilityPort findProductAvailabilityPort;

    @InjectMocks
    private WarehouseCandidateAdapter warehouseCandidateAdapter;

    @Test
    void 전체_창고를_후보로_로드하고_창고별_상품_재고_요약을_매핑한다() {
        // given
        LocalDate expiryA = LocalDate.of(2026, 9, 1);
        LocalDate expiryB = LocalDate.of(2026, 10, 15);
        given(findWarehousePort.findAll()).willReturn(List.of(
                warehouseView(1L, "37.2410000", "127.1775000"),
                warehouseView(2L, "35.1796000", "129.0756000")
        ));
        given(findProductAvailabilityPort.findAvailabilityByProducts(anyCollection())).willReturn(List.of(
                new ProductAvailabilityView(1L, 100L, 30, expiryA),
                new ProductAvailabilityView(1L, 200L, 5, expiryB),
                new ProductAvailabilityView(2L, 100L, 50, expiryB)
        ));

        // when
        List<WarehouseCandidate> candidates = warehouseCandidateAdapter.loadCandidates(List.of(100L, 200L));

        // then
        assertThat(candidates).hasSize(2);

        WarehouseCandidate first = candidates.get(0);
        assertThat(first.getWarehouseId()).isEqualTo(1L);
        assertThat(first.availableQuantityOf(100L)).isEqualTo(30);
        assertThat(first.earliestExpiryOf(100L)).isEqualTo(expiryA);
        assertThat(first.availableQuantityOf(200L)).isEqualTo(5);

        WarehouseCandidate second = candidates.get(1);
        assertThat(second.availableQuantityOf(100L)).isEqualTo(50);
        assertThat(second.availableQuantityOf(200L)).isZero();
    }

    @Test
    void 재고가_전혀_없는_창고도_후보에_포함되며_가용_수량은_0이다() {
        // given
        given(findWarehousePort.findAll()).willReturn(List.of(
                warehouseView(1L, "37.2410000", "127.1775000")
        ));
        given(findProductAvailabilityPort.findAvailabilityByProducts(anyCollection())).willReturn(List.of());

        // when
        List<WarehouseCandidate> candidates = warehouseCandidateAdapter.loadCandidates(List.of(100L));

        // then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.getFirst().hasAnyStock()).isFalse();
    }

    private static WarehouseView warehouseView(Long id, String lat, String lng) {
        return new WarehouseView(id, "창고" + id, "주소" + id,
                new BigDecimal(lat), new BigDecimal(lng));
    }
}
