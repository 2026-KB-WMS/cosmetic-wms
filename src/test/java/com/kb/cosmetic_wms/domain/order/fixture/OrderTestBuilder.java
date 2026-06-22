package com.kb.cosmetic_wms.domain.order.fixture;

import com.kb.cosmetic_wms.domain.order.OrderLine;
import com.kb.cosmetic_wms.domain.order.entity.Orders;

import java.util.ArrayList;
import java.util.List;

public class OrderTestBuilder {
    private Long storeId = 1L;
    private Long warehouseId = 10L;
    private List<OrderLine> orderLines = new ArrayList<>();

    public OrderTestBuilder() {
        this.orderLines.add(new OrderLine(1L, 10));
    }

    public OrderTestBuilder storeId(Long storeId) {
        this.storeId = storeId;
        return this;
    }

    public OrderTestBuilder warehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public OrderTestBuilder orderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
        return this;
    }

    public OrderTestBuilder addOrderLine(Long productId, int quantity) {
        // 🌟 Product 객체 대신 식별자 ID를 받아 OrderLine 조립
        this.orderLines.add(new OrderLine(productId, quantity));
        return this;
    }

    public OrderTestBuilder emptyOrderLines() {
        this.orderLines = new ArrayList<>();
        return this;
    }

    public Orders build() {
        return Orders.create(this.storeId, this.warehouseId, this.orderLines);
    }
}
