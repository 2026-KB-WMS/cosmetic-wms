package com.kb.cosmetic_wms.product.category.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByCategoryCode(String categoryCode);
}