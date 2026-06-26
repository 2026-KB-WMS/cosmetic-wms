package com.kb.cosmetic_wms.order.domain.model;

import lombok.Getter;

@Getter
public class OrderItem {

    private Long id;
    private final Long productId;
    private final int quantity;

    OrderItem(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public static OrderItem reconstitute(Long id, Long productId, int quantity) {
        OrderItem item = new OrderItem(productId, quantity);
        item.id = id;
        return item;
    }
}