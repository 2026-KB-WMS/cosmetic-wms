package com.kb.cosmetic_wms.domain.storage.fixture;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.entity.Section;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.enums.SectionAllocationStatus;
import com.kb.cosmetic_wms.domain.storage.enums.SectionQualityStatus;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;

public class SectionTestBuilder {

    private Warehouse warehouse = new WarehouseTestBuilder().build();
    private String sectionCode = "WH01-HIGH-R-01";
    private String sectionName = "인천 제1 센터 상온 고회전 1구역";
    private SectionType sectionType = SectionType.HIGH_ROT;
    private SectionQualityStatus qualityStatus = SectionQualityStatus.NORMAL;
    private SectionAllocationStatus allocationStatus = SectionAllocationStatus.AVAILABLE;
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

    public SectionTestBuilder qualityStatus(SectionQualityStatus qualityStatus) {
        this.qualityStatus = qualityStatus;
        return this;
    }

    public SectionTestBuilder allocationStatus(SectionAllocationStatus allocationStatus) {
        this.allocationStatus = allocationStatus;
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
        return Section.builder()
                .warehouse(this.warehouse)
                .sectionCode(this.sectionCode)
                .sectionName(this.sectionName)
                .sectionType(this.sectionType)
                .qualityStatus(this.qualityStatus)
                .allocationStatus(this.allocationStatus)
                .temperatureType(this.temperatureType)
                .maxCapacity(this.maxCapacity)
                .build();
    }
}
