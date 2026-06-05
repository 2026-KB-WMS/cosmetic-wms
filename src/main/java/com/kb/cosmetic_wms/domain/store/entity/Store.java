package com.kb.cosmetic_wms.domain.store.entity;

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
public class Store {

    private static final String STORE_NAME_REQUIRED_MESSAGE = "점포명은 필수 입력 항목입니다.";
    private static final String ADDRESS_REQUIRED_MESSAGE = "점포 주소는 필수 입력 항목입니다.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeName;

    private String address;

    private Store(String storeName, String address) {
        this.storeName = storeName;
        this.address = address;
    }

    public static Store create(String storeName, String address) {
        validateStoreName(storeName);
        validateAddress(address);

        return new Store(storeName, address);
    }

    private static void validateStoreName(String storeName) {
        if (storeName == null || storeName.isBlank()) {
            throw new IllegalArgumentException(STORE_NAME_REQUIRED_MESSAGE);
        }
    }

    private static void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException(ADDRESS_REQUIRED_MESSAGE);
        }
    }
}
