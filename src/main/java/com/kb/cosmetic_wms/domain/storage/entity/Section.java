package com.kb.cosmetic_wms.domain.storage.entity;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.constants.StorageConstants;
import com.kb.cosmetic_wms.domain.storage.enums.SectionAllocationStatus;
import com.kb.cosmetic_wms.domain.storage.enums.SectionQualityStatus;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;
import com.kb.cosmetic_wms.domain.storage.exception.StorageValidationException;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "section",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_section_code", columnNames = {"warehouse_id", "section_code"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Section extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_section_warehouse"))
    private Warehouse warehouse;

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

    @Builder(access = AccessLevel.PRIVATE)
    private Section(Warehouse warehouse, String sectionCode, String sectionName,
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

    // 일반 보관 구역 생성 (HIGH_ROT, MID_ROT, LOW_ROT) (품질 상태: NORMAL, 할당 상태: AVAILABLE)
    static Section createStorageSection(
            Warehouse warehouse, String sectionCode, String sectionName,
            SectionType sectionType, TemperatureType temperatureType, int maxCapacity) {

        return Section.builder()
                .warehouse(warehouse)
                .sectionCode(sectionCode)
                .sectionName(sectionName)
                .sectionType(sectionType)
                .qualityStatus(SectionQualityStatus.NORMAL)
                .allocationStatus(SectionAllocationStatus.AVAILABLE)
                .temperatureType(temperatureType)
                .maxCapacity(maxCapacity)
                .build();
    }

    // 검수 대기 구역 생성 (DOCKING) (품질 상태: INSPECTING, 할당 상태: NONE)
    static Section createDockingSection(
            Warehouse warehouse, String sectionCode, String sectionName,
            TemperatureType temperatureType, int maxCapacity) {

        return Section.builder()
                .warehouse(warehouse)
                .sectionCode(sectionCode)
                .sectionName(sectionName)
                .sectionType(SectionType.DOCKING)
                .qualityStatus(SectionQualityStatus.INSPECTING)
                .allocationStatus(SectionAllocationStatus.NONE)
                .temperatureType(temperatureType)
                .maxCapacity(maxCapacity)
                .build();
    }

    // 격리/폐기 구역 생성 (QUARANTINE) (품질 상태: HOLD, 할당 상태: EXCLUDED, 온도 타입: ROOM)
    static Section createQuarantineSection(
            Warehouse warehouse, String sectionCode, String sectionName, int maxCapacity) {

        return Section.builder()
                .warehouse(warehouse)
                .sectionCode(sectionCode)
                .sectionName(sectionName)
                .sectionType(SectionType.QUARANTINE)
                .qualityStatus(SectionQualityStatus.HOLD)
                .allocationStatus(SectionAllocationStatus.EXCLUDED)
                .temperatureType(TemperatureType.ROOM)
                .maxCapacity(maxCapacity)
                .build();
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
            throw new StorageValidationException();
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
