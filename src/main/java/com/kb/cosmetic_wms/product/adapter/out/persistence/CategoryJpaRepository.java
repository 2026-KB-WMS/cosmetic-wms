package com.kb.cosmetic_wms.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByCategoryCode(String categoryCode);
}