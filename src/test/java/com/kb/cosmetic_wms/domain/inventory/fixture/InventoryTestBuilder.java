package com.kb.cosmetic_wms.domain.inventory.fixture;

import com.kb.cosmetic_wms.domain.inventory.entity.Inventory;
import com.kb.cosmetic_wms.domain.inventory.entity.InventoryStatusSet;
import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;

public class InventoryTestBuilder {

    private final Long PRODUCT_ID = 1L;
    private final Long LOT_ID = 10L;
    private final Long WAREHOUSE_ID = 100L;
    private final Long SECTION_ID = 1000L;

    private int quantity = 100;
    private int availableQuantity = 100;

    private AllocStatus allocStatus = AllocStatus.UNALLOCATED;
    private QualityStatus qualityStatus = QualityStatus.NORMAL;
    private LocStatus locStatus = LocStatus.STORED;

    public InventoryTestBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public InventoryTestBuilder availableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
        return this;
    }

    public InventoryTestBuilder allocStatus(AllocStatus allocStatus) {
        this.allocStatus = allocStatus;
        return this;
    }

    public InventoryTestBuilder qualityStatus(QualityStatus qualityStatus) {
        this.qualityStatus = qualityStatus;
        return this;
    }

    public InventoryTestBuilder locStatus(LocStatus locStatus) {
        this.locStatus = locStatus;
        return this;
    }

    public Inventory build() {
        InventoryStatusSet statusSet = InventoryStatusSet.of(
                this.allocStatus, this.qualityStatus, this.locStatus);

        return Inventory.create(
                PRODUCT_ID,
                LOT_ID,
                SECTION_ID,
                WAREHOUSE_ID,
                this.quantity,
                this.availableQuantity,
                statusSet
        );
    }
}
