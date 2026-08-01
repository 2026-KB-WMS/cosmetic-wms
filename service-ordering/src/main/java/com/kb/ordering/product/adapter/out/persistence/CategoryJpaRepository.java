package com.kb.ordering.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByCategoryCode(String categoryCode);
}