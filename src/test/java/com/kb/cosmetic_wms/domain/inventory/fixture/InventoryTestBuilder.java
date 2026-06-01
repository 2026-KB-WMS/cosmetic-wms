package com.kb.cosmetic_wms.domain.inventory.fixture;

import com.kb.cosmetic_wms.domain.inventory.entity.Inventory;
import com.kb.cosmetic_wms.domain.inventory.entity.InventoryStatusSet;
import com.kb.cosmetic_wms.domain.inventory.entity.Lot;
import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.fixture.ProductTestBuilder;
import com.kb.cosmetic_wms.domain.storage.entity.Section;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.fixture.SectionTestBuilder;
import com.kb.cosmetic_wms.domain.storage.fixture.WarehouseTestBuilder;

public class InventoryTestBuilder {

    private Product product = new ProductTestBuilder().build();
    private Lot lot = new LotTestBuilder().product(product).build();
    private Warehouse warehouse = new WarehouseTestBuilder().capacity(10000).build();
    private Section section = new SectionTestBuilder().warehouse(warehouse).maxCapacity(5000).build();

    private int quantity = 100;
    private int availableQuantity = 100;

    private AllocStatus allocStatus = AllocStatus.UNALLOCATED;
    private QualityStatus qualityStatus = QualityStatus.NORMAL;
    private LocStatus locStatus = LocStatus.STORED;

    public InventoryTestBuilder product(Product product) {
        this.product = product;
        return this;
    }

    public InventoryTestBuilder lot(Lot lot) {
        this.lot = lot;
        return this;
    }

    public InventoryTestBuilder warehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
        return this;
    }

    public InventoryTestBuilder section(Section section) {
        this.section = section;
        return this;
    }

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
                this.product,
                this.lot,
                this.section,
                this.warehouse,
                this.quantity,
                this.availableQuantity,
                statusSet
        );
    }
}
