package com.kb.cosmetic_wms.domain.order.entity;

import com.kb.cosmetic_wms.domain.order.OrderLine;
import com.kb.cosmetic_wms.domain.order.enums.OrderStatus;
import com.kb.cosmetic_wms.domain.order.exception.*;
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
        if (storeId == null) throw new OrderStoreRequiredException();
        if (warehouseId == null) throw new OrderWarehouseRequiredException();
        if (lines == null || lines.isEmpty()) throw new OrderItemsRequiredException();

        Orders order = new Orders(storeId, warehouseId);
        lines.forEach(order::addOrderItem);
        return order;
    }

    private void addOrderItem(OrderLine line) {
        this.orderItems.add(new OrderItem(this, line.productId(), line.quantity()));
    }

    public void cancel() {
        if (this.orderStatus != OrderStatus.PENDING) throw new OrderCancelNotAllowedException();
        this.orderStatus = OrderStatus.CANCELED;
    }

    public void confirm() {
        if (this.orderStatus != OrderStatus.PENDING) throw new OrderConfirmNotAllowedException();
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    public void startPreparation() {
        if (this.orderStatus != OrderStatus.CONFIRMED) throw new OrderPreparationNotAllowedException();
        this.orderStatus = OrderStatus.PREPARING;
    }

    public void ship() {
        if (this.orderStatus != OrderStatus.PREPARING) throw new OrderShipNotAllowedException();
        this.orderStatus = OrderStatus.SHIPPED;
    }

    public void completeDelivery() {
        if (this.orderStatus != OrderStatus.SHIPPED) throw new OrderDeliveryCompleteNotAllowedException();
        this.orderStatus = OrderStatus.DELIVERED;
    }
}