package com.kb.ordering.order.domain.model;

import com.kb.ordering.order.domain.enums.OrderStatus;
import com.kb.ordering.order.domain.exception.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Order {

    private Long id;
    private OrderStatus orderStatus;
    private final Long storeId;
    private Long warehouseId;
    private final List<OrderItem> orderItems;

    private Order(Long id, OrderStatus orderStatus, Long storeId, Long warehouseId, List<OrderItem> items) {
        this.id = id;
        this.orderStatus = orderStatus;
        this.storeId = storeId;
        this.warehouseId = warehouseId;
        this.orderItems = new ArrayList<>(items);
    }

    public static Order create(Long storeId, List<OrderLine> lines) {
        if (storeId == null) throw new OrderStoreRequiredException();
        if (lines == null || lines.isEmpty()) throw new OrderItemsRequiredException();

        Order order = new Order(null, OrderStatus.PENDING, storeId, null, List.of());
        lines.forEach(order::addOrderItem);
        return order;
    }

    public static Order reconstitute(Long id, OrderStatus orderStatus, Long storeId, Long warehouseId, List<OrderItem> items) {
        return new Order(id, orderStatus, storeId, warehouseId, items);
    }

    private void addOrderItem(OrderLine line) {
        this.orderItems.add(new OrderItem(line.productId(), line.quantity()));
    }

    public void cancel() {
        if (this.orderStatus != OrderStatus.PENDING) throw new OrderCancelNotAllowedException();
        this.orderStatus = OrderStatus.CANCELED;
    }

    public void confirm() {
        if (this.orderStatus != OrderStatus.PENDING) throw new OrderConfirmNotAllowedException();
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    public void assignWarehouse(Long warehouseId) {
        if (warehouseId == null) throw new OrderWarehouseRequiredException();
        if (this.orderStatus != OrderStatus.CONFIRMED || this.warehouseId != null) {
            throw new OrderWarehouseAssignNotAllowedException();
        }
        this.warehouseId = warehouseId;
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

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }
}