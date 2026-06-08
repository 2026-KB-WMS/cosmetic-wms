package com.kb.cosmetic_wms.domain.order.entity;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    private Long productId;

    private int quantity;

    OrderItem(Order order, Long productId, int quantity) {
        this.order = order;
        this.productId = productId;
        this.quantity = quantity;
    }
}
