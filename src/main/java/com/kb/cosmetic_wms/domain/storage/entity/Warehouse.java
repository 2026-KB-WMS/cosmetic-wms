package com.kb.cosmetic_wms.domain.storage.entity;

import com.kb.cosmetic_wms.domain.storage.constants.StorageConstants;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String warehouseName;
    private String address;
    private String targetTemp;
    private int capacity;

    private Warehouse(String warehouseName, String address, String targetTemp, int capacity) {
        this.warehouseName = warehouseName;
        this.address = address;
        this.targetTemp = targetTemp;
        this.capacity = capacity;
    }

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Section> sections = new ArrayList<>();

    public static Warehouse create(String warehouseName, String address, String targetTemp, int capacity) {
        validateWarehouseName(warehouseName);
        validateAddress(address);
        validateTargetTemp(targetTemp);
        validateCapacity(capacity);

        return new Warehouse(warehouseName, address, targetTemp, capacity);
    }

    public void addSection(Section section) {
        validateDuplicateSectionCode(section.getSectionCode());
        validateTotalSectionCapacity(section.getMaxCapacity());

        this.sections.add(section);
        section.assignWarehouse(this);
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
            throw new IllegalArgumentException(StorageConstants.EXCEED_WAREHOUSE_CAPACITY_MESSAGE);
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
