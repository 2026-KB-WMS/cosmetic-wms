package com.kb.cosmetic_wms.domain.storage.fixture;

import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;

public class WarehouseTestBuilder {

    private String warehouseName = "인천 제1 센터";
    private String address = "인천광역시 중구";
    private String targetTemp = "10~25도";
    private int capacity = 10000;

    public WarehouseTestBuilder warehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
        return this;
    }

    public WarehouseTestBuilder address(String address) {
        this.address = address;
        return this;
    }

    public WarehouseTestBuilder targetTemp(String targetTemp) {
        this.targetTemp = targetTemp;
        return this;
    }

    public WarehouseTestBuilder capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public Warehouse build() {
        return Warehouse.create(
                warehouseName,
                address,
                targetTemp,
                capacity
        );
    }
}
