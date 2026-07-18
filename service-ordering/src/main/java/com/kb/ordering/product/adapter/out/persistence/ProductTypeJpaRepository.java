package com.kb.ordering.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTypeJpaRepository extends JpaRepository<ProductTypeEntity, Long> {
    boolean existsByTypeCode(String typeCode);
}
