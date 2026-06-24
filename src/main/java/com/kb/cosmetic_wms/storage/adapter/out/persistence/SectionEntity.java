package com.kb.cosmetic_wms.storage.adapter.out.persistence;

import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.storage.domain.model.Section;
import com.kb.cosmetic_wms.storage.domain.model.SectionAllocationStatus;
import com.kb.cosmetic_wms.storage.domain.model.SectionCode;
import com.kb.cosmetic_wms.storage.domain.model.SectionQualityStatus;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "section",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_section_code", columnNames = {"warehouse_id", "section_code"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class SectionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_section_warehouse"))
    private WarehouseEntity warehouse;

    @Column(name = "section_code", nullable = false, length = 20)
    private String sectionCode;

    @Column(name = "section_name", nullable = false, length = 50)
    private String sectionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 20)
    private SectionType sectionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality_status", nullable = false, length = 10)
    private SectionQualityStatus qualityStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_status", nullable = false, length = 10)
    private SectionAllocationStatus allocationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "temperature_type", nullable = false, length = 10)
    private TemperatureType temperatureType;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "current_capacity", nullable = false)
    private int currentCapacity;

    private SectionEntity(Long id, WarehouseEntity warehouse, String sectionCode, String sectionName,
                          SectionType sectionType, SectionQualityStatus qualityStatus,
                          SectionAllocationStatus allocationStatus, TemperatureType temperatureType,
                          int maxCapacity, int currentCapacity) {
        this.id = id;
        this.warehouse = warehouse;
        this.sectionCode = sectionCode;
        this.sectionName = sectionName;
        this.sectionType = sectionType;
        this.qualityStatus = qualityStatus;
        this.allocationStatus = allocationStatus;
        this.temperatureType = temperatureType;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = currentCapacity;
    }

    static SectionEntity fromDomain(Section domain, WarehouseEntity warehouse) {
        return new SectionEntity(
                domain.getSectionId(),
                warehouse,
                domain.getSectionCode().value(),
                domain.getSectionName(),
                domain.getSectionType(),
                domain.getQualityStatus(),
                domain.getAllocationStatus(),
                domain.getTemperatureType(),
                domain.getMaxCapacity(),
                domain.getCurrentCapacity()
        );
    }

    Section toDomain() {
        return Section.reconstitute(id, new SectionCode(sectionCode), sectionName, sectionType,
                qualityStatus, allocationStatus, temperatureType, maxCapacity, currentCapacity);
    }
}