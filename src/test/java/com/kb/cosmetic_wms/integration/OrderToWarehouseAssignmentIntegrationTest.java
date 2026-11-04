package com.kb.cosmetic_wms.integration;

import org.springframework.test.context.TestPropertySource;
import com.kb.cosmetic_wms.support.MySqlContainerSupport;
import com.kb.cosmetic_wms.support.TestAuthentication;
import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.global.geocoding.GeocodingPort;
import com.kb.cosmetic_wms.oms.application.port.in.FailedAssignmentResult;
import com.kb.cosmetic_wms.oms.application.port.in.FindFailedAssignmentUseCase;
import com.kb.cosmetic_wms.oms.application.port.in.ResolveFailedAssignmentUseCase;
import com.kb.cosmetic_wms.oms.application.port.out.RoutingPort;
import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.exception.FailedAssignmentNotPendingException;
import com.kb.cosmetic_wms.oms.domain.exception.OmsErrorCode;
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

import com.kb.cosmetic_wms.inventory.domain.model.ShelfLifePolicy;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
@TestPropertySource(properties = "spring.datasource.hikari.connection-init-sql=SET FOREIGN_KEY_CHECKS=0")
class OrderToWarehouseAssignmentIntegrationTest extends MySqlContainerSupport {

    private static final Long PRODUCT_ID = 100L;
    private static final long ACTOR_ID = 7777L;
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

    @Autowired
    private Clock clock;

    @Autowired
    private ShelfLifePolicy shelfLifePolicy;

    @Autowired
    private FindFailedAssignmentUseCase findFailedAssignmentUseCase;

    @Autowired
    private ResolveFailedAssignmentUseCase resolveFailedAssignmentUseCase;

    private Long nearWarehouseId;
    private Long farWarehouseId;
    private Long storeId;

    @BeforeEach
    void setUp() {
        // 마스터 FK 체인(product·lot·section) 세팅 대신 클래스의 FOREIGN_KEY_CHECKS=0 설정으로 참조 무결성을 해제하고 재고를 직접 삽입한다
        cleanUp();
        TestAuthentication.authenticateAs(ACTOR_ID);

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
        TestAuthentication.clear();
        cleanUp();
    }

    @Test
    void 발주_확정_시_최적_창고가_자동_배정되고_출고_전표_생성과_재고_할당까지_완료된다() {
        // given — 두 창고 모두 전량 충족 가능, 김포가 배송지에서 가까움
        insertInventory(nearWarehouseId, PRODUCT_ID, 100, expiryAfter(60));
        insertInventory(farWarehouseId, PRODUCT_ID, 100, expiryAfter(60));

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

        // 비동기 배정 체인에서도 발주를 확정한 로그인 사용자가 재고 이력의 행위자로 기록된다
        Integer actorTransactions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_transaction WHERE member_id = ?", Integer.class, ACTOR_ID);
        Integer otherActorTransactions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_transaction WHERE member_id <> ?", Integer.class, ACTOR_ID);
        assertThat(actorTransactions).isPositive();
        assertThat(otherActorTransactions).isZero();
    }

    @Test
    void 전량_충족_창고가_없으면_배정_실패가_기록되고_발주는_창고_미배정_CONFIRMED_상태로_남는다() {
        // given — 요청 수량(30)보다 적은 재고만 존재
        insertInventory(nearWarehouseId, PRODUCT_ID, 10, expiryAfter(60));

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

    private record FailedOrder(Long orderId, Long failedEventId) {
    }

    /**
     * 재고가 부족한 상태에서 발주를 확정해 창고 배정 실패 건을 만든다.
     */
    private FailedOrder createFailedAssignment() {
        insertInventory(nearWarehouseId, PRODUCT_ID, 10, expiryAfter(60));
        OrderResult order = orderLifecycleUseCase.createOrder(new CreateOrderCommand(storeId,
                List.of(new CreateOrderCommand.OrderLineCommand(PRODUCT_ID, 30))));
        orderLifecycleUseCase.confirmOrder(order.id());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM failed_assignment_event WHERE orders_id = ?", Integer.class, order.id())).isEqualTo(1));

        Long failedEventId = jdbcTemplate.queryForObject(
                "SELECT failed_event_id FROM failed_assignment_event WHERE orders_id = ?", Long.class, order.id());
        return new FailedOrder(order.id(), failedEventId);
    }

    @Test
    void 기록된_실패_건은_처리_대기_상태로_조회된다() {
        FailedOrder failed = createFailedAssignment();

        List<FailedAssignmentResult> pending = findFailedAssignmentUseCase.findByStatus(FailedEventStatus.PENDING);

        assertThat(pending).extracting(FailedAssignmentResult::id).containsExactly(failed.failedEventId());
        assertThat(pending.get(0).retryCount()).isZero();
    }

    @Test
    void 재고를_보충한_뒤_재처리하면_창고_배정과_출고_전표_생성과_재고_할당까지_완료되고_해결_처리된다() {
        // given
        FailedOrder failed = createFailedAssignment();
        insertInventory(nearWarehouseId, PRODUCT_ID, 100, expiryAfter(60));

        // when
        FailedAssignmentResult result = resolveFailedAssignmentUseCase.retry(failed.failedEventId());

        // then
        assertThat(result.status()).isEqualTo(FailedEventStatus.RESOLVED);
        assertThat(result.retryCount()).isEqualTo(1);

        Map<String, Object> orderRow = jdbcTemplate.queryForMap(
                "SELECT order_status, warehouse_id FROM orders WHERE orders_id = ?", failed.orderId());
        assertThat(orderRow.get("order_status")).isEqualTo("PREPARING");
        assertThat(((Number) orderRow.get("warehouse_id")).longValue()).isEqualTo(nearWarehouseId);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT outbound_status FROM outbound WHERE orders_id = ?", String.class, failed.orderId()))
                .isEqualTo("ALLOCATED");

        Map<String, Object> failedRow = jdbcTemplate.queryForMap(
                "SELECT status, retry_count, resolved_at FROM failed_assignment_event WHERE failed_event_id = ?",
                failed.failedEventId());
        assertThat(failedRow.get("status")).isEqualTo("RESOLVED");
        assertThat(((Number) failedRow.get("retry_count")).intValue()).isEqualTo(1);
        assertThat(failedRow.get("resolved_at")).isNotNull();
    }

    @Test
    void 해결된_건을_다시_재처리하면_예외를_던지고_출고_전표를_중복_생성하지_않는다() {
        // given
        FailedOrder failed = createFailedAssignment();
        insertInventory(nearWarehouseId, PRODUCT_ID, 100, expiryAfter(60));
        resolveFailedAssignmentUseCase.retry(failed.failedEventId());

        // when & then
        assertThatThrownBy(() -> resolveFailedAssignmentUseCase.retry(failed.failedEventId()))
                .isInstanceOf(FailedAssignmentNotPendingException.class);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbound WHERE orders_id = ?", Integer.class, failed.orderId())).isEqualTo(1);
    }

    @Test
    void 재고가_여전히_부족하면_재처리가_실패해도_예외_없이_대기_상태로_시도_횟수와_사유가_남고_발주는_그대로다() {
        // given
        FailedOrder failed = createFailedAssignment();

        // when
        FailedAssignmentResult result = resolveFailedAssignmentUseCase.retry(failed.failedEventId());

        // then — 별도 트랜잭션의 롤백이 실패 기록까지 되돌리지 않는다
        assertThat(result.status()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(result.retryCount()).isEqualTo(1);
        assertThat(result.errorMessage()).isEqualTo(OmsErrorCode.NO_ASSIGNABLE_WAREHOUSE.getMessage());

        Map<String, Object> failedRow = jdbcTemplate.queryForMap(
                "SELECT status, retry_count FROM failed_assignment_event WHERE failed_event_id = ?",
                failed.failedEventId());
        assertThat(failedRow.get("status")).isEqualTo("PENDING");
        assertThat(((Number) failedRow.get("retry_count")).intValue()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT order_status FROM orders WHERE orders_id = ?", String.class, failed.orderId()))
                .isEqualTo("CONFIRMED");
    }

    @Test
    void 이미_창고가_배정된_발주는_배정을_시도하지_않고_사유를_남기며_폐기할_수_있다() {
        // given — 실패 후 다른 경로로 창고가 먼저 배정된 발주
        FailedOrder failed = createFailedAssignment();
        orderLifecycleUseCase.assignWarehouse(failed.orderId(), nearWarehouseId);

        // when
        FailedAssignmentResult retried = resolveFailedAssignmentUseCase.retry(failed.failedEventId());
        FailedAssignmentResult discarded = resolveFailedAssignmentUseCase.discard(failed.failedEventId());

        // then
        assertThat(retried.status()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(retried.errorMessage()).isEqualTo(OmsErrorCode.ORDER_NOT_AWAITING_ASSIGNMENT.getMessage());
        assertThat(discarded.status()).isEqualTo(FailedEventStatus.DISCARDED);
        assertThat(findFailedAssignmentUseCase.findByStatus(FailedEventStatus.PENDING)).isEmpty();
    }

    @Test
    void 창고_배정에_실패한_확정_발주를_취소하면_취소되고_이후_재처리는_사유를_남기며_폐기할_수_있다() {
        // given — 재고 부족으로 배정에 실패해 CONFIRMED에 머문 발주
        FailedOrder failed = createFailedAssignment();

        // when
        orderLifecycleUseCase.cancelOrder(failed.orderId());
        FailedAssignmentResult retried = resolveFailedAssignmentUseCase.retry(failed.failedEventId());
        FailedAssignmentResult discarded = resolveFailedAssignmentUseCase.discard(failed.failedEventId());

        // then
        assertThat(jdbcTemplate.queryForObject(
                "SELECT order_status FROM orders WHERE orders_id = ?", String.class, failed.orderId()))
                .isEqualTo("CANCELED");
        assertThat(retried.status()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(retried.errorMessage()).isEqualTo(OmsErrorCode.ORDER_NOT_AWAITING_ASSIGNMENT.getMessage());
        assertThat(discarded.status()).isEqualTo(FailedEventStatus.DISCARDED);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbound WHERE orders_id = ?", Integer.class, failed.orderId())).isZero();
    }

    @Test
    void 운영_설정의_출고_가능_최소_잔여_유통기한은_7일이다() {
        assertThat(shelfLifePolicy.minRemainingDays()).isEqualTo(7);
    }

    private String expiryAfter(int days) {
        return LocalDate.now(clock).plusDays(days).toString();
    }

    @Test
    void 유통기한이_임박한_재고만_있는_창고는_더_가까워도_배정_후보에서_제외된다() {
        // given — 가까운 창고는 잔여 3일 재고만, 먼 창고는 잔여 60일 재고
        insertInventory(nearWarehouseId, PRODUCT_ID, 100, expiryAfter(3));
        insertInventory(farWarehouseId, PRODUCT_ID, 100, expiryAfter(60));

        OrderResult order = orderLifecycleUseCase.createOrder(new CreateOrderCommand(storeId,
                List.of(new CreateOrderCommand.OrderLineCommand(PRODUCT_ID, 30))));

        // when
        orderLifecycleUseCase.confirmOrder(order.id());

        // then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Map<String, Object> orderRow = jdbcTemplate.queryForMap(
                    "SELECT order_status, warehouse_id FROM orders WHERE orders_id = ?", order.id());
            assertThat(orderRow.get("order_status")).isEqualTo("PREPARING");
            assertThat(((Number) orderRow.get("warehouse_id")).longValue()).isEqualTo(farWarehouseId);
        });
        assertThat(jdbcTemplate.queryForObject(
                "SELECT SUM(quantity) FROM inventory WHERE warehouse_id = ? AND alloc_status = 'UNALLOCATED'",
                Integer.class, nearWarehouseId)).isEqualTo(100);
    }

    @Test
    void 같은_창고에_임박_재고와_정상_재고가_있으면_출고_할당은_정상_재고에서만_이루어진다() {
        // given — 임박 재고가 유통기한이 더 빠르지만 FEFO 후보에서 제외되어야 한다
        insertInventory(nearWarehouseId, PRODUCT_ID, 50, expiryAfter(5));
        insertInventory(nearWarehouseId, PRODUCT_ID, 100, expiryAfter(60));

        OrderResult order = orderLifecycleUseCase.createOrder(new CreateOrderCommand(storeId,
                List.of(new CreateOrderCommand.OrderLineCommand(PRODUCT_ID, 30))));

        // when
        orderLifecycleUseCase.confirmOrder(order.id());

        // then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(jdbcTemplate.queryForObject(
                "SELECT order_status FROM orders WHERE orders_id = ?", String.class, order.id()))
                .isEqualTo("PREPARING"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT SUM(quantity) FROM inventory WHERE warehouse_id = ? AND alloc_status = 'ALLOCATED' AND expiry_date = ?",
                Integer.class, nearWarehouseId, expiryAfter(60))).isEqualTo(30);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT SUM(quantity) FROM inventory WHERE warehouse_id = ? AND expiry_date = ? AND alloc_status = 'UNALLOCATED'",
                Integer.class, nearWarehouseId, expiryAfter(5))).isEqualTo(50);
    }

    @Test
    void 모든_재고가_만료되었거나_임박하면_배정에_실패하고_실패_기록이_남는다() {
        // given
        insertInventory(nearWarehouseId, PRODUCT_ID, 100, expiryAfter(-10));
        insertInventory(farWarehouseId, PRODUCT_ID, 100, expiryAfter(7));

        OrderResult order = orderLifecycleUseCase.createOrder(new CreateOrderCommand(storeId,
                List.of(new CreateOrderCommand.OrderLineCommand(PRODUCT_ID, 30))));

        // when
        orderLifecycleUseCase.confirmOrder(order.id());

        // then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM failed_assignment_event WHERE orders_id = ?", Integer.class, order.id()))
                .isEqualTo(1));
        assertThat(jdbcTemplate.queryForObject(
                "SELECT order_status FROM orders WHERE orders_id = ?", String.class, order.id())).isEqualTo("CONFIRMED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbound WHERE orders_id = ?", Integer.class, order.id())).isZero();
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
