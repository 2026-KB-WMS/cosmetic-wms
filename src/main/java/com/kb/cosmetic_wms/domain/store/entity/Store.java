package com.kb.cosmetic_wms.domain.store.entity;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "store",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_store_name_address", columnNames = {"store_name", "address"})
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Store extends BaseEntity {

    private static final String STORE_NAME_REQUIRED_MESSAGE = "점포명은 필수 입력 항목입니다.";
    private static final String ADDRESS_REQUIRED_MESSAGE = "점포 주소는 필수 입력 항목입니다.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "address", nullable = false, length = 100)
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
