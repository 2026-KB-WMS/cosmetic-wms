package com.kb.cosmetic_wms.integration;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.global.geocoding.GeocodingPort;
import com.kb.cosmetic_wms.oms.application.port.out.RoutingPort;
import com.kb.cosmetic_wms.oms.domain.service.HaversineDistanceCalculator;
import com.kb.cosmetic_wms.order.application.port.in.CreateOrderCommand;
import com.kb.cosmetic_wms.order.application.port.in.OrderLifecycleUseCase;
import com.kb.cosmetic_wms.order.application.port.in.OrderResult;
import com.kb.cosmetic_wms.storage.application.port.in.RegisterWarehouseCommand;
import com.kb.cosmetic_wms.storage.application.port.in.RegisterWarehouseUseCase;
import com.kb.cosmetic_wms.store.application.port.in.RegisterStoreCommand;
import com.kb.cosmetic_wms.store.application.port.in.RegisterStoreUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * 발주 확정(OrderConfirmedEvent) → 최적 창고 자동 배정(oms) → WarehouseAssignedEvent →
 * 출고 전표 생성·FEFO 재고 할당(outbound) → 발주 PREPARING 전환까지의 전체 체인을 검증한다.
 *
 * <pre>
 * [발주 확정 - confirmOrder()]  ...트랜잭션 T1 커밋
 *   └─ AFTER_COMMIT + @Async: oms RetryableWarehouseAssigner → AssignWarehouseService ...새 트랜잭션 T2
 *       ├─ 창고 확정 (Order.assignWarehouse)
 *       ├─ WarehouseAssignedEvent 발행
 *       └─ BEFORE_COMMIT (동기): OutboundEventHandler → 출고 전표 + 재고 할당 + startPreparation
 *          → 배정·출고·할당이 T2 하나로 묶여 원자적으로 처리된다
 * </pre>
 */
@SpringBootTest
class OrderToWarehouseAssignmentIntegrationTest {

    private static final Long PRODUCT_ID = 100L;
    private static final GeoCoordinate STORE_SEOUL = GeoCoordinate.of(37.5665, 126.9780);
    private static final GeoCoordinate WAREHOUSE_GIMPO = GeoCoordinate.of(37.6152, 126.7159);
    private static final GeoCoordinate WAREHOUSE_BUSAN = GeoCoordinate.of(35.1798, 129.0750);

    @MockitoBean
    private GeocodingPort geocodingPort;

    @MockitoBean
    private RoutingPort routingPort;

    @Autowired
    private RegisterWarehouseUseCase registerWarehouseUseCase;
    @Autowired
    private RegisterStoreUseCase registerStoreUseCase;
    @Autowired
    private OrderLifecycleUseCase orderLifecycleUseCase;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long nearWarehouseId;
    private Long farWarehouseId;
    private Long storeId;

    @BeforeEach
    void setUp() {
        // 마스터 FK 체인(product·lot·section) 세팅 대신 참조 무결성을 해제하고 재고를 직접 삽입한다
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        cleanUp();

        Map<String, GeoCoordinate> coordinatesByAddress = Map.of(
                "서울시 중구", STORE_SEOUL,
                "경기도 김포시", WAREHOUSE_GIMPO,
                "부산광역시 강서구", WAREHOUSE_BUSAN
        );
        given(geocodingPort.geocode(anyString()))
                .willAnswer(inv -> coordinatesByAddress.get(inv.getArgument(0, String.class)));
        given(routingPort.drivingDistanceMeters(any(), any()))
                .willAnswer(inv -> Math.round(HaversineDistanceCalculator.distanceMeters(
                        inv.getArgument(0), inv.getArgument(1)) * 1.3));

        nearWarehouseId = registerWarehouseUseCase
                .register(new RegisterWarehouseCommand("김포 센터", "경기도 김포시", "10~25도", 10000))
                .warehouseId();
        farWarehouseId = registerWarehouseUseCase
                .register(new RegisterWarehouseCommand("부산 센터", "부산광역시 강서구", "10~25도", 10000))
                .warehouseId();
        storeId = registerStoreUseCase
                .register(new RegisterStoreCommand("서울 중구점", "서울시 중구"))
                .storeId();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void 발주_확정_시_최적_창고가_자동_배정되고_출고_전표_생성과_재고_할당까지_완료된다() {
        // given — 두 창고 모두 전량 충족 가능, 김포가 배송지에서 가까움
        insertInventory(nearWarehouseId, PRODUCT_ID, 100, "2026-12-31");
        insertInventory(farWarehouseId, PRODUCT_ID, 100, "2026-12-31");

        OrderResult order = orderLifecycleUseCase.createOrder(new CreateOrderCommand(storeId,
                List.of(new CreateOrderCommand.OrderLineCommand(PRODUCT_ID, 30))));

        // when
        orderLifecycleUseCase.confirmOrder(order.id());

        // then — 비동기 배정 체인 완료 대기
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Map<String, Object> orderRow = jdbcTemplate.queryForMap(
                    "SELECT order_status, warehouse_id FROM orders WHERE orders_id = ?", order.id());
            assertThat(orderRow.get("order_status")).isEqualTo("PREPARING");
            assertThat(((Number) orderRow.get("warehouse_id")).longValue()).isEqualTo(nearWarehouseId);
        });

        Map<String, Object> outboundRow = jdbcTemplate.queryForMap(
                "SELECT warehouse_id, outbound_status FROM outbound WHERE orders_id = ?", order.id());
        assertThat(((Number) outboundRow.get("warehouse_id")).longValue()).isEqualTo(nearWarehouseId);
        assertThat(outboundRow.get("outbound_status")).isEqualTo("ALLOCATED");

        Integer allocatedQuantity = jdbcTemplate.queryForObject(
                "SELECT SUM(quantity) FROM inventory WHERE warehouse_id = ? AND alloc_status = 'ALLOCATED'",
                Integer.class, nearWarehouseId);
        assertThat(allocatedQuantity).isEqualTo(30);

        // 원본 재고 행이 차감되고 전체 수량이 보존되는지 검증 (재고 부풀림 회귀 방지)
        Integer unallocatedQuantity = jdbcTemplate.queryForObject(
                "SELECT SUM(quantity) FROM inventory WHERE warehouse_id = ? AND alloc_status = 'UNALLOCATED'",
                Integer.class, nearWarehouseId);
        assertThat(unallocatedQuantity).isEqualTo(70);

        Integer totalQuantity = jdbcTemplate.queryForObject(
                "SELECT SUM(quantity) FROM inventory WHERE warehouse_id = ?", Integer.class, nearWarehouseId);
        assertThat(totalQuantity).isEqualTo(100);

        Integer untouchedQuantity = jdbcTemplate.queryForObject(
                "SELECT SUM(quantity) FROM inventory WHERE warehouse_id = ? AND alloc_status = 'UNALLOCATED'",
                Integer.class, farWarehouseId);
        assertThat(untouchedQuantity).isEqualTo(100);
    }

    @Test
    void 전량_충족_창고가_없으면_배정_실패가_기록되고_발주는_창고_미배정_CONFIRMED_상태로_남는다() {
        // given — 요청 수량(30)보다 적은 재고만 존재
        insertInventory(nearWarehouseId, PRODUCT_ID, 10, "2026-12-31");

        OrderResult order = orderLifecycleUseCase.createOrder(new CreateOrderCommand(storeId,
                List.of(new CreateOrderCommand.OrderLineCommand(PRODUCT_ID, 30))));

        // when
        orderLifecycleUseCase.confirmOrder(order.id());

        // then — 실패 이벤트 기록 대기
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Integer failedCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM failed_assignment_event WHERE orders_id = ?",
                    Integer.class, order.id());
            assertThat(failedCount).isEqualTo(1);
        });

        Map<String, Object> orderRow = jdbcTemplate.queryForMap(
                "SELECT order_status, warehouse_id FROM orders WHERE orders_id = ?", order.id());
        assertThat(orderRow.get("order_status")).isEqualTo("CONFIRMED");
        assertThat(orderRow.get("warehouse_id")).isNull();

        Integer outboundCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbound WHERE orders_id = ?", Integer.class, order.id());
        assertThat(outboundCount).isZero();
    }

    private long lotSeq = 1;

    private void insertInventory(Long warehouseId, Long productId, int quantity, String expiryDate) {
        jdbcTemplate.update("""
                        INSERT INTO inventory (product_id, lot_id, section_id, warehouse_id, quantity,
                                               available_quantity, alloc_status, quality_status, loc_status,
                                               expiry_date, created_by, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, 'UNALLOCATED', 'NORMAL', 'STORED', ?, 1, NOW())
                        """,
                productId, lotSeq++, lotSeq, warehouseId, quantity, quantity, expiryDate);
    }

    private void cleanUp() {
        for (String table : List.of("outbound_item", "outbound", "inventory_transaction", "inventory",
                "orders_item", "orders", "failed_assignment_event", "section", "warehouse", "store")) {
            jdbcTemplate.execute("DELETE FROM " + table);
        }
    }
}
