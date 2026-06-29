package com.kb.cosmetic_wms.storage.domain.model;

import com.kb.cosmetic_wms.storage.domain.exception.DuplicateSectionCodeException;
import com.kb.cosmetic_wms.storage.domain.exception.SectionCapacityOverflowException;
import com.kb.cosmetic_wms.storage.domain.exception.StorageErrorCode;
import com.kb.cosmetic_wms.storage.domain.exception.StorageExceedCapacityException;
import com.kb.cosmetic_wms.storage.domain.exception.StorageValidationException;
import com.kb.cosmetic_wms.storage.domain.service.SectionCodeGenerator;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Warehouse {

    private final Long warehouseId;
    private final String warehouseName;
    private final String address;
    private final TargetTemp targetTemp;
    private final int capacity;
    private final List<Section> sections;

    private Warehouse(Long warehouseId, String warehouseName, String address,
                      TargetTemp targetTemp, int capacity, List<Section> sections) {
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.address = address;
        this.targetTemp = targetTemp;
        this.capacity = capacity;
        this.sections = sections;
    }

    public static Warehouse create(String warehouseName, String address, String targetTemp, int capacity) {
        validateWarehouseName(warehouseName);
        validateAddress(address);
        validateCapacity(capacity);
        return new Warehouse(null, warehouseName, address, new TargetTemp(targetTemp), capacity, new ArrayList<>());
    }

    public static Warehouse reconstitute(Long warehouseId, String warehouseName, String address,
                                         String targetTemp, int capacity, List<Section> sections) {
        return new Warehouse(warehouseId, warehouseName, address,
                new TargetTemp(targetTemp), capacity, new ArrayList<>(sections));
    }

    public String getTargetTempValue() {
        return targetTemp.value();
    }

    public Section addSection(SectionType sectionType, String sectionName,
                              TemperatureZone temperatureType, int maxCapacity) {
        if (warehouseId == null) {
            throw new StorageValidationException(StorageErrorCode.INVALID_STORAGE_STATE);
        }
        int sequence = nextSequenceFor(sectionType);
        String warehouseCode = String.format("WH%02d", warehouseId);
        TemperatureZone resolvedTemp = sectionType == SectionType.QUARANTINE ? TemperatureZone.ROOM : temperatureType;
        SectionCode code = new SectionCodeGenerator().generate(warehouseCode, sectionType, resolvedTemp, sequence);

        return switch (sectionType) {
            case DOCKING -> addDockingSection(code, sectionName, temperatureType, maxCapacity);
            case QUARANTINE -> addQuarantineSection(code, sectionName, maxCapacity);
            default -> addStorageSection(code, sectionName, sectionType, temperatureType, maxCapacity);
        };
    }

    private int nextSequenceFor(SectionType sectionType) {
        return (int) sections.stream()
                .filter(s -> s.getSectionType() == sectionType)
                .count() + 1;
    }

    public Section addStorageSection(SectionCode sectionCode, String sectionName,
                                     SectionType sectionType, TemperatureZone temperatureType,
                                     int maxCapacity) {
        validateDuplicateSectionCode(sectionCode);
        validateTotalSectionCapacity(maxCapacity);
        Section section = Section.createStorageSection(sectionCode, sectionName, sectionType, temperatureType, maxCapacity);
        sections.add(section);
        return section;
    }

    public Section addDockingSection(SectionCode sectionCode, String sectionName,
                                     TemperatureZone temperatureType, int maxCapacity) {
        validateDuplicateSectionCode(sectionCode);
        validateTotalSectionCapacity(maxCapacity);
        Section section = Section.createDockingSection(sectionCode, sectionName, temperatureType, maxCapacity);
        sections.add(section);
        return section;
    }

    public Section addQuarantineSection(SectionCode sectionCode, String sectionName, int maxCapacity) {
        validateDuplicateSectionCode(sectionCode);
        validateTotalSectionCapacity(maxCapacity);
        Section section = Section.createQuarantineSection(sectionCode, sectionName, maxCapacity);
        sections.add(section);
        return section;
    }

    public void receiveToDocking(Map<TemperatureZone, Integer> receivedByZone) {
        for (Map.Entry<TemperatureZone, Integer> entry : receivedByZone.entrySet()) {
            int remaining = entry.getValue();
            for (Section section : getDockingSectionsByZone(entry.getKey())) {
                if (remaining <= 0) break;
                int available = section.getMaxCapacity() - section.getCurrentCapacity();
                int toAdd = Math.min(remaining, available);
                section.plusCapacity(toAdd);
                remaining -= toAdd;
            }
            if (remaining > 0) {
                throw new SectionCapacityOverflowException();
            }
        }
    }

    private List<Section> getDockingSectionsByZone(TemperatureZone zone) {
        return sections.stream()
                .filter(s -> s.getSectionType() == SectionType.DOCKING && s.getTemperatureType() == zone)
                .toList();
    }

    public boolean canAccommodateDocking(Map<TemperatureZone, Integer> requiredByZone) {
        Map<TemperatureZone, Integer> availableByZone = sections.stream()
                .filter(s -> s.getSectionType() == SectionType.DOCKING)
                .collect(Collectors.groupingBy(
                        Section::getTemperatureType,
                        Collectors.summingInt(s -> s.getMaxCapacity() - s.getCurrentCapacity())
                ));
        return requiredByZone.entrySet().stream()
                .allMatch(e -> availableByZone.getOrDefault(e.getKey(), 0) >= e.getValue());
    }

    private void validateDuplicateSectionCode(SectionCode sectionCode) {
        boolean isDuplicate = this.sections.stream()
                .anyMatch(s -> s.getSectionCode().equals(sectionCode));
        if (isDuplicate) {
            throw new DuplicateSectionCodeException();
        }
    }

    private void validateTotalSectionCapacity(int newSectionMaxCapacity) {
        int currentTotal = this.sections.stream().mapToInt(Section::getMaxCapacity).sum();
        if (currentTotal + newSectionMaxCapacity > this.capacity) {
            throw new StorageExceedCapacityException();
        }
    }

    private static void validateWarehouseName(String warehouseName) {
        if (warehouseName == null || warehouseName.isBlank()) {
            throw new StorageValidationException(StorageErrorCode.INVALID_WAREHOUSE_NAME);
        }
    }

    private static void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new StorageValidationException(StorageErrorCode.INVALID_ADDRESS);
        }
    }

    private static void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new StorageValidationException(StorageErrorCode.INVALID_CAPACITY);
        }
    }
}
