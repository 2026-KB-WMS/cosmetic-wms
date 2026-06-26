package com.kb.cosmetic_wms.storage.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.storage.domain.model.Section;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "warehouse")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class WarehouseEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Long id;

    @Column(name = "warehouse_name", nullable = false, length = 100)
    private String warehouseName;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "target_temp", nullable = false, length = 20)
    private String targetTemp;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<SectionEntity> sections = new ArrayList<>();

    private WarehouseEntity(Long id, String warehouseName, String address, String targetTemp, int capacity) {
        this.id = id;
        this.warehouseName = warehouseName;
        this.address = address;
        this.targetTemp = targetTemp;
        this.capacity = capacity;
    }

    static WarehouseEntity fromDomain(Warehouse domain) {
        WarehouseEntity entity = new WarehouseEntity(
                domain.getWarehouseId(),
                domain.getWarehouseName(),
                domain.getAddress(),
                domain.getTargetTempValue(),
                domain.getCapacity()
        );
        domain.getSections().stream()
                .map(section -> SectionEntity.fromDomain(section, entity))
                .forEach(entity.sections::add);
        return entity;
    }

    Warehouse toDomain() {
        List<Section> domainSections = sections.stream()
                .map(SectionEntity::toDomain)
                .toList();
        return Warehouse.reconstitute(id, warehouseName, address, targetTemp, capacity, domainSections);
    }
}