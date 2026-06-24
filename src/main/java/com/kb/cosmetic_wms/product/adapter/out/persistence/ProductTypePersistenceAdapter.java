package com.kb.cosmetic_wms.product.adapter.out.persistence;

import com.kb.cosmetic_wms.product.application.port.out.ProductTypePort;
import com.kb.cosmetic_wms.product.domain.model.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductTypePersistenceAdapter implements ProductTypePort {

    private final ProductTypeJpaRepository productTypeJpaRepository;

    @Override
    public Optional<ProductType> findById(Long productTypeId) {
        return productTypeJpaRepository.findById(productTypeId)
                .map(ProductTypeEntity::toDomain);
    }
}