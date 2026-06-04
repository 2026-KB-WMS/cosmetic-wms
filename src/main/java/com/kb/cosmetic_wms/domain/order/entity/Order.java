package com.kb.cosmetic_wms.domain.order.entity;

import com.kb.cosmetic_wms.domain.order.OrderLine;
import com.kb.cosmetic_wms.domain.order.constants.OrderConstants;
import com.kb.cosmetic_wms.domain.order.enums.OrderStatus;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> orderItems = new ArrayList<>();

    private Order(Store store, Warehouse warehouse) {
        this.orderStatus = OrderStatus.PENDING;
        this.store = store;
        this.warehouse = warehouse;
    }

    /**
     * 발주를 생성한다.
     *
     * <p>생성된 발주는 항상 PENDING 상태이며
     * 전달된 OrderLine 목록을 기반으로 OrderItem을 조립한다.
     *
     * @param store     발주를 요청한 가맹점 정보
     * @param warehouse 시스템 정책에 의해 할당된 배정 창고 정보
     * @param lines     점주가 요청한 상품 및 수량 리스트
     * @return 무결성이 보장된 발주 객체
     */
    public static Order create(Store store, Warehouse warehouse, List<OrderLine> lines) {
        validateStore(store);
        validateWarehouse(warehouse);
        validateOrderLine(lines);

        Order order = new Order(store, warehouse);
        lines.forEach(order::addOrderItem);
        return order;
    }

    private void addOrderItem(OrderLine line) {
        this.orderItems.add(new OrderItem(this, line.product(), line.quantity()));
    }

    /**
     * 진행 중인 발주 요청을 취소 처리한다.
     *
     * <p>오직 창고 작업이 시작되기 전인 '발주 대기(PENDING)' 상태에서만 취소가 가능하다.
     *
     * @throws IllegalStateException 현재 발주 상태가 PENDING(대기)이 아닐 경우 발생
     */
    public void cancel() {
        if (this.orderStatus != OrderStatus.PENDING) {
            throw new IllegalStateException(OrderConstants.INVALID_CANCEL_STATUS_MESSAGE);
        }
        this.orderStatus = OrderStatus.CANCELED;
    }

    /**
     * 발주에 대한 창고 내 물류 작업을 시작한다.
     *
     * <p>창고 관리자가 발주 요청을 접수하여 재고 할당, 피킹 및 패킹을 하는 시점에 호출된다.
     * 이 단계부터는 점주가 시스템에서 임의로 발주를 취소할 수 없다.
     *
     * @throws IllegalStateException 현재 발주 상태가 PENDING(대기)이 아닐 경우 발생
     */
    public void startOrderProcess() {
        if (this.orderStatus != OrderStatus.PENDING) {
            throw new IllegalStateException(OrderConstants.INVALID_START_STATUS_MESSAGE);
        }
        this.orderStatus = OrderStatus.IN_PROGRESS;
    }

    /**
     * 물품들에 대한 배송을 시작한다.
     *
     * <p>'작업 중(IN_PROGRESS)' 상태의 주문만 배송 중 상태로 변경될 수 있다.</p>
     *
     * @throws IllegalStateException 현재 발주 상태가 IN_PROGRESS(작업 중)가 아닐 경우 발생
     */
    public void ship() {
        if (this.orderStatus != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(OrderConstants.INVALID_SHIP_STATUS_MESSAGE);
        }
        this.orderStatus = OrderStatus.SHIPPED;
    }

    /**
     * 배송 완료로 전체 발주가 종료된다.
     *
     * @throws IllegalStateException 현재 발주 상태가 SHIPPED(배송 중)가 아닐 경우 발생
     */
    public void completeDelivery() {
        if (this.orderStatus != OrderStatus.SHIPPED) {
            throw new IllegalStateException(OrderConstants.INVALID_DELIVERY_STATUS_MESSAGE);
        }
        this.orderStatus = OrderStatus.DELIVERED;
    }

    private static void validateStore(Store store) {
        if (store == null) {
            throw new IllegalArgumentException(OrderConstants.STORE_REQUIRED_MESSAGE);
        }
    }

    private static void validateWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException(OrderConstants.WAREHOUSE_REQUIRED_MESSAGE);
        }
    }

    private static void validateOrderLine(List<OrderLine> lines) {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException(OrderConstants.ORDER_ITEM_MINIMUM_MESSAGE);
        }
    }
}
