package com.kb.cosmetic_wms.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductTypeJpaRepository extends JpaRepository<ProductTypeEntity, Long> {
    boolean existsByTypeCode(String typeCode);
}