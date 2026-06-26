package com.kb.cosmetic_wms.product.producttype.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTypeJpaRepository extends JpaRepository<ProductTypeEntity, Long> {
    boolean existsByTypeCode(String typeCode);
}
