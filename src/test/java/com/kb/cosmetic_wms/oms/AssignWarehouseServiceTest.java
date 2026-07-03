package com.kb.cosmetic_wms.oms;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.oms.application.port.in.AssignWarehouseCommand;
import com.kb.cosmetic_wms.oms.application.port.in.WarehouseAssignmentResult;
import com.kb.cosmetic_wms.oms.application.port.out.LoadStoreLocationPort;
import com.kb.cosmetic_wms.oms.application.port.out.LoadWarehouseCandidatesPort;
import com.kb.cosmetic_wms.oms.application.port.out.RoutingPort;
import com.kb.cosmetic_wms.oms.application.service.AssignWarehouseService;
import com.kb.cosmetic_wms.oms.domain.event.WarehouseAssignedEvent;
import com.kb.cosmetic_wms.oms.domain.exception.NoAssignableWarehouseException;
import com.kb.cosmetic_wms.oms.domain.model.ProductStock;
import com.kb.cosmetic_wms.oms.domain.model.WarehouseCandidate;
import com.kb.cosmetic_wms.oms.domain.service.HaversineDistanceCalculator;
import com.kb.cosmetic_wms.oms.domain.service.WeightBasedAssignmentPolicy;
import com.kb.cosmetic_wms.oms.application.port.out.AssignOrderWarehousePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssignWarehouseServiceTest {

    private static final GeoCoordinate STORE_LOCATION = GeoCoordinate.of(37.5665, 126.9780); // 서울
    private static final GeoCoordinate NEAR_WAREHOUSE = GeoCoordinate.of(37.6152, 126.7159); // 김포
    private static final GeoCoordinate FAR_WAREHOUSE = GeoCoordinate.of(35.1798, 129.0750);  // 부산
    private static final LocalDate EXPIRY = LocalDate.now().plusDays(180);

    @Mock
    private LoadStoreLocationPort loadStoreLocationPort;

    @Mock
    private LoadWarehouseCandidatesPort loadWarehouseCandidatesPort;

    @Mock
    private RoutingPort routingPort;

    @Mock
    private AssignOrderWarehousePort assignOrderWarehousePort;

    @Mock
    private EventPublisher eventPublisher;

    private AssignWarehouseService assignWarehouseService;

    @BeforeEach
    void setUp() {
        assignWarehouseService = new AssignWarehouseService(
                loadStoreLocationPort, loadWarehouseCandidatesPort, routingPort,
                WeightBasedAssignmentPolicy.withDefaults(), assignOrderWarehousePort, eventPublisher);

        lenient().when(routingPort.drivingDistanceMeters(any(), any()))
                .thenAnswer(inv -> Math.round(HaversineDistanceCalculator.distanceMeters(
                        inv.getArgument(0), inv.getArgument(1)) * 1.3));
    }

    @Test
    void 최적_창고를_선정해_발주에_확정하고_WarehouseAssignedEvent를_발행한다() {
        // given
        AssignWarehouseCommand command = new AssignWarehouseCommand(1L, 10L,
                List.of(new AssignWarehouseCommand.ItemDemand(5L, 100L, 30)));
        given(loadStoreLocationPort.loadStoreLocation(10L)).willReturn(STORE_LOCATION);
        given(loadWarehouseCandidatesPort.loadCandidates(anyCollection())).willReturn(List.of(
                WarehouseCandidate.of(1L, NEAR_WAREHOUSE, Map.of(100L, new ProductStock(100, EXPIRY))),
                WarehouseCandidate.of(2L, FAR_WAREHOUSE, Map.of(100L, new ProductStock(100, EXPIRY)))
        ));

        // when
        WarehouseAssignmentResult result = assignWarehouseService.assign(command);

        // then — 가까운 창고(1L) 선정, 발주 확정 및 이벤트 발행
        assertThat(result.orderId()).isEqualTo(1L);
        assertThat(result.warehouseId()).isEqualTo(1L);
        verify(assignOrderWarehousePort).assignWarehouse(1L, 1L);

        ArgumentCaptor<WarehouseAssignedEvent> captor = ArgumentCaptor.forClass(WarehouseAssignedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        WarehouseAssignedEvent event = captor.getValue();
        assertThat(event.orderId()).isEqualTo(1L);
        assertThat(event.warehouseId()).isEqualTo(1L);
        assertThat(event.items()).hasSize(1);
        assertThat(event.items().getFirst().orderItemId()).isEqualTo(5L);
        assertThat(event.items().getFirst().productId()).isEqualTo(100L);
        assertThat(event.items().getFirst().quantity()).isEqualTo(30);
    }

    @Test
    void 전량_충족_창고가_없으면_발주_확정과_이벤트_발행_없이_예외를_던진다() {
        // given
        AssignWarehouseCommand command = new AssignWarehouseCommand(1L, 10L,
                List.of(new AssignWarehouseCommand.ItemDemand(5L, 100L, 500)));
        given(loadStoreLocationPort.loadStoreLocation(10L)).willReturn(STORE_LOCATION);
        given(loadWarehouseCandidatesPort.loadCandidates(anyCollection())).willReturn(List.of(
                WarehouseCandidate.of(1L, NEAR_WAREHOUSE, Map.of(100L, new ProductStock(10, EXPIRY)))
        ));

        // when & then
        assertThatThrownBy(() -> assignWarehouseService.assign(command))
                .isInstanceOf(NoAssignableWarehouseException.class);

        verify(assignOrderWarehousePort, never()).assignWarehouse(anyLong(), anyLong());
        verify(eventPublisher, never()).publish(any());
    }
}
