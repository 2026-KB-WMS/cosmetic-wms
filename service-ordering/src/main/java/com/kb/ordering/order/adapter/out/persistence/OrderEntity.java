package com.kb.ordering.order.adapter.out.persistence;

import com.kb.common.jpa.BaseEntity;
import com.kb.ordering.order.domain.enums.OrderStatus;
import com.kb.ordering.order.domain.model.Order;
import com.kb.ordering.order.domain.model.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    private OrderEntity(Long id, OrderStatus orderStatus, Long storeId, Long warehouseId) {
        this.id = id;
        this.orderStatus = orderStatus;
        this.storeId = storeId;
        this.warehouseId = warehouseId;
    }

    static OrderEntity fromDomain(Order order) {
        OrderEntity entity = new OrderEntity(order.getId(), order.getOrderStatus(), order.getStoreId(), order.getWarehouseId());
        order.getOrderItems().forEach(item -> {
            OrderItemEntity itemEntity = OrderItemEntity.fromDomain(item);
            itemEntity.setOrder(entity);
            entity.orderItems.add(itemEntity);
        });
        return entity;
    }

    Order toDomain() {
        List<OrderItem> items = orderItems.stream()
                .map(OrderItemEntity::toDomain)
                .toList();
        return Order.reconstitute(id, orderStatus, storeId, warehouseId, items);
    }
}