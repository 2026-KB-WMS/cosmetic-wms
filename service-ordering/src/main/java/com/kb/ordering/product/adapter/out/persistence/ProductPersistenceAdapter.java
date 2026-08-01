package com.kb.ordering.product.adapter.out.persistence;

import com.kb.ordering.product.application.port.out.ProductPort;
import com.kb.ordering.product.domain.product.enums.TemperatureType;
import com.kb.ordering.product.domain.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductPort {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public boolean existsDuplicateProduct(String brandName, String productName,
                                          Long categoryId, Long productTypeId,
                                          int volumeValue, String volumeUnit) {
        return productJpaRepository.existsDuplicateProduct(
                brandName, productName, categoryId, productTypeId, volumeValue, volumeUnit);
    }

    @Override
    public boolean existsById(Long productId) {
        return productJpaRepository.existsById(productId);
    }

    @Override
    public boolean allExistByIds(Collection<Long> productIds) {
        Set<Long> uniqueIds = new HashSet<>(productIds);
        return productJpaRepository.countByIdIn(uniqueIds) == uniqueIds.size();
    }

    @Override
    public Map<Long, TemperatureType> findTemperatureTypesByIds(Collection<Long> productIds) {
        return productJpaRepository.findIdAndTemperatureTypeByIdIn(productIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (TemperatureType) row[1]
                ));
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        return productJpaRepository.existsByCategoryId(categoryId);
    }

    @Override
    public boolean existsByProductTypeId(Long productTypeId) {
        return productJpaRepository.existsByProductTypeId(productTypeId);
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId)
                .map(ProductEntity::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll().stream()
                .map(ProductEntity::toDomain)
                .toList();
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(ProductEntity.fromDomain(product)).toDomain();
    }

    @Override
    public void delete(Product product) {
        productJpaRepository.deleteById(product.getProductId());
    }
}
