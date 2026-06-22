package com.kb.cosmetic_wms.domain.order.entity;

import com.kb.cosmetic_wms.domain.order.OrderLine;
import com.kb.cosmetic_wms.domain.order.constants.OrderConstants;
import com.kb.cosmetic_wms.domain.order.enums.OrderStatus;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> orderItems = new ArrayList<>();

    private Orders(Long storeId, Long warehouseId) {
        this.orderStatus = OrderStatus.PENDING;
        this.storeId = storeId;
        this.warehouseId = warehouseId;
    }

    public static Orders create(Long storeId, Long warehouseId, List<OrderLine> lines) {
        validateStore(storeId);
        validateWarehouse(warehouseId);
        validateOrderLine(lines);

        Orders order = new Orders(storeId, warehouseId);
        lines.forEach(order::addOrderItem);
        return order;
    }

    private void addOrderItem(OrderLine line) {
        this.orderItems.add(new OrderItem(this, line.productId(), line.quantity()));
    }

    public void cancel() {
        if (this.orderStatus != OrderStatus.PENDING) {
            throw new IllegalStateException(OrderConstants.INVALID_CANCEL_STATUS_MESSAGE);
        }
        this.orderStatus = OrderStatus.CANCELED;
    }

    public void startOrderProcess() {
        if (this.orderStatus != OrderStatus.PENDING) {
            throw new IllegalStateException(OrderConstants.INVALID_START_STATUS_MESSAGE);
        }
        this.orderStatus = OrderStatus.IN_PROGRESS;
    }

    public void ship() {
        if (this.orderStatus != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(OrderConstants.INVALID_SHIP_STATUS_MESSAGE);
        }
        this.orderStatus = OrderStatus.SHIPPED;
    }

    public void completeDelivery() {
        if (this.orderStatus != OrderStatus.SHIPPED) {
            throw new IllegalStateException(OrderConstants.INVALID_DELIVERY_STATUS_MESSAGE);
        }
        this.orderStatus = OrderStatus.DELIVERED;
    }

    private static void validateStore(Long storeId) {
        if (storeId == null) {
            throw new IllegalArgumentException(OrderConstants.STORE_REQUIRED_MESSAGE);
        }
    }

    private static void validateWarehouse(Long warehouseId) {
        if (warehouseId == null) {
            throw new IllegalArgumentException(OrderConstants.WAREHOUSE_REQUIRED_MESSAGE);
        }
    }

    private static void validateOrderLine(List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(OrderConstants.ORDER_ITEM_MINIMUM_MESSAGE);
        }
    }
}