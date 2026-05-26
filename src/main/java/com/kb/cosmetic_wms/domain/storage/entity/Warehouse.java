package com.kb.cosmetic_wms.domain.storage.entity;

import com.kb.cosmetic_wms.domain.storage.constants.StorageConstants;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public static Warehouse create(String warehouseName, String address, String targetTemp, int capacity) {
        validateWarehouseName(warehouseName);
        validateAddress(address);
        validateTargetTemp(targetTemp);
        validateCapacity(capacity);

        return new Warehouse(warehouseName, address, targetTemp, capacity);
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
