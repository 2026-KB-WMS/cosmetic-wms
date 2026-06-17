package com.kb.cosmetic_wms.domain.product.repository;

import com.kb.cosmetic_wms.domain.product.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
}
