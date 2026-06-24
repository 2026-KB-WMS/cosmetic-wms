package com.kb.cosmetic_wms.domain.storage.fixture;

import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.storage.domain.model.Section;
import com.kb.cosmetic_wms.storage.domain.model.SectionCode;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;

public class SectionTestBuilder {

    private Warehouse warehouse = new WarehouseTestBuilder().build();
    private String sectionCode = "WH01-HIGH-R-01";
    private String sectionName = "인천 제1 센터 상온 고회전 1구역";
    private SectionType sectionType = SectionType.HIGH_ROT;
    private TemperatureType temperatureType = TemperatureType.ROOM;
    private int maxCapacity = 5000;

    public SectionTestBuilder warehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
        return this;
    }

    public SectionTestBuilder sectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
        return this;
    }

    public SectionTestBuilder sectionName(String sectionName) {
        this.sectionName = sectionName;
        return this;
    }

    public SectionTestBuilder sectionType(SectionType sectionType) {
        this.sectionType = sectionType;
        return this;
    }

    public SectionTestBuilder temperatureType(TemperatureType temperatureType) {
        this.temperatureType = temperatureType;
        return this;
    }

    public SectionTestBuilder maxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        return this;
    }

    public Section build() {
        SectionCode code = new SectionCode(this.sectionCode);
        if (this.sectionType == SectionType.DOCKING) {
            return this.warehouse.addDockingSection(code, this.sectionName, this.temperatureType, this.maxCapacity);
        }
        if (this.sectionType == SectionType.QUARANTINE) {
            return this.warehouse.addQuarantineSection(code, this.sectionName, this.maxCapacity);
        }
        return this.warehouse.addStorageSection(code, this.sectionName, this.sectionType, this.temperatureType, this.maxCapacity);
    }
}