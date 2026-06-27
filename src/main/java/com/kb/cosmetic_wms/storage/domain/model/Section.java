package com.kb.cosmetic_wms.storage.domain.model;

import com.kb.cosmetic_wms.storage.domain.exception.SectionCapacityOverflowException;
import com.kb.cosmetic_wms.storage.domain.exception.SectionCapacityUnderflowException;
import com.kb.cosmetic_wms.storage.domain.exception.StorageErrorCode;
import com.kb.cosmetic_wms.storage.domain.exception.StorageValidationException;
import lombok.Getter;

@Getter
public class Section {

    private final Long sectionId;
    private final SectionCode sectionCode;
    private final String sectionName;
    private final SectionType sectionType;
    private final SectionQualityStatus qualityStatus;
    private final SectionAllocationStatus allocationStatus;
    private final TemperatureZone temperatureType;
    private final int maxCapacity;
    private int currentCapacity;

    private Section(Long sectionId, SectionCode sectionCode, String sectionName,
                    SectionType sectionType, SectionQualityStatus qualityStatus,
                    SectionAllocationStatus allocationStatus, TemperatureZone temperatureType,
                    int maxCapacity, int currentCapacity) {
        this.sectionId = sectionId;
        this.sectionCode = sectionCode;
        this.sectionName = sectionName;
        this.sectionType = sectionType;
        this.qualityStatus = qualityStatus;
        this.allocationStatus = allocationStatus;
        this.temperatureType = temperatureType;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = currentCapacity;
    }

    static Section createStorageSection(SectionCode sectionCode, String sectionName,
                                        SectionType sectionType, TemperatureZone temperatureType,
                                        int maxCapacity) {
        validateMaxCapacity(maxCapacity);
        validateSectionName(sectionName);
        validateStateCombination(sectionType, SectionQualityStatus.NORMAL, SectionAllocationStatus.AVAILABLE);
        return new Section(null, sectionCode, sectionName, sectionType,
                SectionQualityStatus.NORMAL, SectionAllocationStatus.AVAILABLE,
                temperatureType, maxCapacity, 0);
    }

    static Section createDockingSection(SectionCode sectionCode, String sectionName,
                                        TemperatureZone temperatureType, int maxCapacity) {
        validateMaxCapacity(maxCapacity);
        validateSectionName(sectionName);
        return new Section(null, sectionCode, sectionName, SectionType.DOCKING,
                SectionQualityStatus.INSPECTING, SectionAllocationStatus.NONE,
                temperatureType, maxCapacity, 0);
    }

    static Section createQuarantineSection(SectionCode sectionCode, String sectionName, int maxCapacity) {
        validateMaxCapacity(maxCapacity);
        validateSectionName(sectionName);
        return new Section(null, sectionCode, sectionName, SectionType.QUARANTINE,
                SectionQualityStatus.HOLD, SectionAllocationStatus.EXCLUDED,
                TemperatureZone.ROOM, maxCapacity, 0);

    }

    public static Section reconstitute(Long sectionId, SectionCode sectionCode, String sectionName,
                                       SectionType sectionType, SectionQualityStatus qualityStatus,
                                       SectionAllocationStatus allocationStatus,
                                       TemperatureZone temperatureType, int maxCapacity, int currentCapacity) {
        return new Section(sectionId, sectionCode, sectionName, sectionType,
                qualityStatus, allocationStatus, temperatureType, maxCapacity, currentCapacity);
    }

    public void plusCapacity(int quantity) {
        if (this.currentCapacity + quantity > this.maxCapacity) {
            throw new SectionCapacityOverflowException();
        }
        this.currentCapacity += quantity;
    }

    public void minusCapacity(int quantity) {
        if (this.currentCapacity - quantity < 0) {
            throw new SectionCapacityUnderflowException();
        }
        this.currentCapacity -= quantity;
    }

    private static void validateMaxCapacity(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new StorageValidationException(StorageErrorCode.INVALID_SECTION_MAX_CAPACITY);
        }
    }

    private static void validateSectionName(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            throw new StorageValidationException(StorageErrorCode.INVALID_SECTION_NAME);
        }
    }

    private static void validateStateCombination(SectionType type, SectionQualityStatus quality,
                                                 SectionAllocationStatus allocation) {
        if (type == SectionType.DOCKING) {
            if (quality != SectionQualityStatus.INSPECTING || allocation != SectionAllocationStatus.NONE) {
                throw new StorageValidationException(StorageErrorCode.INVALID_STORAGE_STATE);
            }
        }
        if (type == SectionType.QUARANTINE) {
            if (quality != SectionQualityStatus.HOLD || allocation != SectionAllocationStatus.EXCLUDED) {
                throw new StorageValidationException(StorageErrorCode.INVALID_STORAGE_STATE);
            }
        }
        if (type == SectionType.HIGH_ROT || type == SectionType.MID_ROT || type == SectionType.LOW_ROT) {
            if (quality == SectionQualityStatus.INSPECTING || allocation == SectionAllocationStatus.NONE) {
                throw new StorageValidationException(StorageErrorCode.INVALID_STORAGE_STATE);
            }
        }
    }
}