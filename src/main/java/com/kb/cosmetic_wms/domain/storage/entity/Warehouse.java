package com.kb.cosmetic_wms.domain.storage.entity;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.constants.StorageConstants;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;
import com.kb.cosmetic_wms.domain.storage.exception.StorageExceedCapacityException;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "warehouse")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Warehouse extends BaseEntity {

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
    private final List<Section> sections = new ArrayList<>();

    private Warehouse(String warehouseName, String address, String targetTemp, int capacity) {
        this.warehouseName = warehouseName;
        this.address = address;
        this.targetTemp = targetTemp;
        this.capacity = capacity;
    }

    public static Warehouse create(String warehouseName, String address, String targetTemp, int capacity) {
        validateWarehouseName(warehouseName);
        validateAddress(address);
        validateTargetTemp(targetTemp);
        validateCapacity(capacity);

        return new Warehouse(warehouseName, address, targetTemp, capacity);
    }

    // 일반 보관 구역 생성 및 추가 (HIGH_ROT, MID_ROT, LOW_ROT)
    public Section addStorageSection(
            String sectionCode, String sectionName, SectionType sectionType,
            TemperatureType temperatureType, int maxCapacity) {

        validateDuplicateSectionCode(sectionCode);
        validateTotalSectionCapacity(maxCapacity);

        Section section = Section.createStorageSection(
                this, sectionCode, sectionName, sectionType, temperatureType, maxCapacity
        );

        this.sections.add(section);
        return section;
    }

    // 검수 대기 구역 생성 및 추가 (DOCKING)
    public Section addDockingSection(
            String sectionCode, String sectionName, TemperatureType temperatureType, int maxCapacity) {

        validateDuplicateSectionCode(sectionCode);
        validateTotalSectionCapacity(maxCapacity);

        Section section = Section.createDockingSection(
                this, sectionCode, sectionName, temperatureType, maxCapacity
        );

        this.sections.add(section);
        return section;
    }

    // 격리/폐기 구역 생성 및 추가 (QUARANTINE)
    public Section addQuarantineSection(String sectionCode, String sectionName, int maxCapacity) {

        validateDuplicateSectionCode(sectionCode);
        validateTotalSectionCapacity(maxCapacity);

        Section section = Section.createQuarantineSection(
                this, sectionCode, sectionName, maxCapacity
        );

        this.sections.add(section);
        return section;
    }

    private void validateDuplicateSectionCode(String sectionCode) {
        boolean isDuplicate = this.sections.stream()
                .anyMatch(existingSection -> existingSection.getSectionCode().equals(sectionCode));

        if (isDuplicate) {
            throw new IllegalArgumentException(StorageConstants.DUPLICATE_SECTION_CODE_MESSAGE);
        }
    }

    private void validateTotalSectionCapacity(int newSectionMaxCapacity) {
        int currentTotalCapacity = this.sections.stream()
                .mapToInt(Section::getMaxCapacity)
                .sum();

        if (currentTotalCapacity + newSectionMaxCapacity > this.capacity) {
            throw new StorageExceedCapacityException();
        }
    }

    private static void validateWarehouseName(String warehouseName) {
        if (warehouseName == null || warehouseName.isBlank()) {
            throw new IllegalArgumentException(StorageConstants.WAREHOUSE_NAME_REQUIRED_MESSAGE);
        }
    }

    private static void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException(StorageConstants.ADDRESS_REQUIRED_MESSAGE);
        }
    }

    private static void validateTargetTemp(String targetTemp) {
        if (targetTemp == null || targetTemp.isBlank()) {
            throw new IllegalArgumentException(StorageConstants.TARGET_TEMP_REQUIRED_MESSAGE);
        }
        if (!StorageConstants.TEMP_PATTERN.matcher(targetTemp.trim()).matches()) {
            throw new IllegalArgumentException(StorageConstants.INVALID_TARGET_TEMP_PATTERN_MESSAGE);
        }
    }

    private static void validateCapacity(int capacity) {
        if (capacity <= StorageConstants.MIN_CAPACITY_BOUND) {
            throw new IllegalArgumentException(StorageConstants.INVALID_CAPACITY_MESSAGE);
        }
    }
}
