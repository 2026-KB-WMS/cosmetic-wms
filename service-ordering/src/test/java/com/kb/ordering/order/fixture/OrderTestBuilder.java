package com.kb.ordering.order.fixture;

import com.kb.ordering.order.domain.model.Order;
import com.kb.ordering.order.domain.model.OrderLine;

import java.util.ArrayList;
import java.util.List;

public class OrderTestBuilder {

    private Long storeId = 1L;
    private List<OrderLine> orderLines = new ArrayList<>();

    public OrderTestBuilder() {
        this.orderLines.add(new OrderLine(1L, 10));
    }

    public OrderTestBuilder storeId(Long storeId) {
        this.storeId = storeId;
        return this;
    }

    public OrderTestBuilder orderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
        return this;
    }

    public OrderTestBuilder addOrderLine(Long productId, int quantity) {
        this.orderLines.add(new OrderLine(productId, quantity));
        return this;
    }

    public OrderTestBuilder emptyOrderLines() {
        this.orderLines = new ArrayList<>();
        return this;
    }

    public Order build() {
        return Order.create(this.storeId, this.orderLines);
    }
}
