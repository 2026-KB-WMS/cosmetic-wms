package com.kb.ordering.order.adapter.out.persistence;

import com.kb.common.jpa.BaseEntity;
import com.kb.ordering.order.domain.model.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_item_orders"))
    private OrderEntity order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    private OrderItemEntity(Long id, Long productId, int quantity) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
    }

    static OrderItemEntity fromDomain(OrderItem item) {
        return new OrderItemEntity(item.getId(), item.getProductId(), item.getQuantity());
    }

    void setOrder(OrderEntity order) {
        this.order = order;
    }

    OrderItem toDomain() {
        return OrderItem.reconstitute(id, productId, quantity);
    }
}