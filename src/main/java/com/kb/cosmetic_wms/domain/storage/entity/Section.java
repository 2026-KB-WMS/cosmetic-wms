package com.kb.cosmetic_wms.domain.storage.entity;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.constants.StorageConstants;
import com.kb.cosmetic_wms.domain.storage.enums.SectionAllocationStatus;
import com.kb.cosmetic_wms.domain.storage.enums.SectionQualityStatus;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    private String sectionCode;
    private String sectionName;

    @Enumerated(EnumType.STRING)
    private SectionType sectionType;

    @Enumerated(EnumType.STRING)
    private SectionQualityStatus qualityStatus;

    @Enumerated(EnumType.STRING)
    private SectionAllocationStatus allocationStatus;

    @Enumerated(EnumType.STRING)
    private TemperatureType temperatureType;

    private int maxCapacity;

    private int currentCapacity;

    @Builder
    protected Section(Warehouse warehouse, String sectionCode, String sectionName,
                      SectionType sectionType, SectionQualityStatus qualityStatus,
                      SectionAllocationStatus allocationStatus, TemperatureType temperatureType,
                      int maxCapacity) {

        validateMaxCapacity(maxCapacity);
        validateSectionCode(sectionCode);
        validateSectionName(sectionName);
        validateStateCombination(sectionType, qualityStatus, allocationStatus);
        validateQuarantineTemperature(sectionType, temperatureType);

        this.warehouse = warehouse;
        this.sectionCode = sectionCode;
        this.sectionName = sectionName;
        this.sectionType = sectionType;
        this.qualityStatus = qualityStatus;
        this.allocationStatus = allocationStatus;
        this.temperatureType = temperatureType;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = 0;
    }

    protected void assignWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    private void validateMaxCapacity(int maxCapacity) {
        if (maxCapacity <= StorageConstants.MIN_CAPACITY_BOUND) {
            throw new IllegalArgumentException(StorageConstants.INVALID_SECTION_MAX_CAPACITY_MESSAGE);
        }
    }

    private void validateSectionCode(String sectionCode) {
        if (sectionCode == null || sectionCode.isBlank()) {
            throw new IllegalArgumentException(StorageConstants.SECTION_CODE_REQUIRED_MESSAGE);
        }
        if (!StorageConstants.SECTION_CODE_PATTERN.matcher(sectionCode.trim()).matches()) {
            throw new IllegalArgumentException(StorageConstants.INVALID_SECTION_CODE_PATTERN_MESSAGE);
        }
    }

    private void validateSectionName(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            throw new IllegalArgumentException(StorageConstants.SECTION_NAME_REQUIRED_MESSAGE);
        }
    }

    private void validateStateCombination(
            SectionType type, SectionQualityStatus quality, SectionAllocationStatus allocation
    ) {
        // 검수 대기 구역 제약 조건
        if (type == SectionType.DOCKING) {
            if (quality != SectionQualityStatus.INSPECTING || allocation != SectionAllocationStatus.NONE) {
                throw new IllegalArgumentException(StorageConstants.INVALID_SECTION_STATE_COMBINATION_MESSAGE);
            }
        }
        // 격리/폐기 구역 제약 조건
        if (type == SectionType.QUARANTINE) {
            if (quality != SectionQualityStatus.HOLD || allocation != SectionAllocationStatus.EXCLUDED) {
                throw new IllegalArgumentException(StorageConstants.INVALID_SECTION_STATE_COMBINATION_MESSAGE);
            }
        }
        // 일반 보관 구역 제약 조건
        if (type == SectionType.HIGH_ROT || type == SectionType.MID_ROT || type == SectionType.LOW_ROT) {
            if (quality == SectionQualityStatus.INSPECTING || allocation == SectionAllocationStatus.NONE) {
                throw new IllegalArgumentException(StorageConstants.INVALID_SECTION_STATE_COMBINATION_MESSAGE);
            }
        }
    }

    private void validateQuarantineTemperature(SectionType sectionType, TemperatureType temperatureType) {
        if (sectionType == SectionType.QUARANTINE && temperatureType == TemperatureType.COOL) {
            throw new IllegalArgumentException(StorageConstants.QUARANTINE_MUST_BE_ROOM_MESSAGE);
        }
    }

    public void plusCapacity(int quantity) {
        if (this.currentCapacity + quantity > this.maxCapacity) {
            throw new IllegalArgumentException(StorageConstants.SECTION_CAPACITY_OVERFLOW_MESSAGE);
        }
        this.currentCapacity += quantity;
    }

    public void minusCapacity(int quantity) {
        if (this.currentCapacity - quantity < StorageConstants.MIN_CAPACITY_BOUND) {
            throw new IllegalArgumentException(StorageConstants.SECTION_CAPACITY_UNDERFLOW_MESSAGE);
        }
        this.currentCapacity -= quantity;
    }
}
