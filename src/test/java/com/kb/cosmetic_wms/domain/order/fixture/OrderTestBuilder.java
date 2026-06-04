package com.kb.cosmetic_wms.domain.order.fixture;

import com.kb.cosmetic_wms.domain.order.OrderLine;
import com.kb.cosmetic_wms.domain.order.entity.Order;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.fixture.ProductTestBuilder;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.fixture.WarehouseTestBuilder;
import com.kb.cosmetic_wms.domain.store.entity.Store;

import java.util.ArrayList;
import java.util.List;

public class OrderTestBuilder {
    private Store store = Store.create("기본 가맹점", "기본 주소");
    private Warehouse warehouse = new WarehouseTestBuilder().build();
    private List<OrderLine> orderLines = new ArrayList<>();

    public OrderTestBuilder() {
        Product defaultProduct = new ProductTestBuilder().build();
        this.orderLines.add(new OrderLine(defaultProduct, 10));
    }

    public OrderTestBuilder store(Store store) {
        this.store = store;
        return this;
    }

    public OrderTestBuilder warehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
        return this;
    }

    public OrderTestBuilder orderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
        return this;
    }

    public OrderTestBuilder addOrderLine(Product product, int quantity) {
        this.orderLines.add(new OrderLine(product, quantity));
        return this;
    }

    public OrderTestBuilder emptyOrderLines() {
        this.orderLines = new ArrayList<>();
        return this;
    }

    public Order build() {
        return Order.create(this.store, this.warehouse, this.orderLines);
    }
}
