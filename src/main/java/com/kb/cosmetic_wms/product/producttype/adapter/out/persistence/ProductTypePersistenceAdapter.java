package com.kb.cosmetic_wms.product.producttype.adapter.out.persistence;

import com.kb.cosmetic_wms.product.producttype.application.port.out.ProductTypePort;
import com.kb.cosmetic_wms.product.producttype.domain.model.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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

    @Override
    public List<ProductType> findAll() {
        return productTypeJpaRepository.findAll().stream()
                .map(ProductTypeEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCode(String typeCode) {
        return productTypeJpaRepository.existsByTypeCode(typeCode);
    }

    @Override
    public ProductType save(ProductType productType) {
        return productTypeJpaRepository.save(ProductTypeEntity.fromDomain(productType)).toDomain();
    }

    @Override
    public void deleteById(Long productTypeId) {
        productTypeJpaRepository.deleteById(productTypeId);
    }
}
