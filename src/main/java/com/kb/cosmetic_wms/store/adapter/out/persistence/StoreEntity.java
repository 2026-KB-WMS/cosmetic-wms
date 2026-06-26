package com.kb.cosmetic_wms.store.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.store.domain.model.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "store",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_store_name_address", columnNames = {"store_name", "address"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StoreEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "address", nullable = false, length = 100)
    private String address;

    private StoreEntity(String storeName, String address) {
        this.storeName = storeName;
        this.address = address;
    }

    static StoreEntity fromDomain(Store store) {
        return new StoreEntity(store.getStoreName(), store.getAddress());
    }

    Store toDomain() {
        return Store.reconstitute(id, storeName, address);
    }
}