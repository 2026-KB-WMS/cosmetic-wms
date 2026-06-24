package com.kb.cosmetic_wms.product.adapter.out.persistence;

import com.kb.cosmetic_wms.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductPort {

    private final ProductJpaRepository productJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final ProductTypeJpaRepository productTypeJpaRepository;

    @Override
    public boolean existsDuplicateProduct(String brandName, String productName,
                                          Long categoryId, Long productTypeId,
                                          int volumeValue, String volumeUnit) {
        return productJpaRepository.existsDuplicateProduct(
                brandName, productName, categoryId, productTypeId, volumeValue, volumeUnit);
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        return productJpaRepository.existsByCategory_Id(categoryId);
    }

    @Override
    public boolean existsByProductTypeId(Long productTypeId) {
        return productJpaRepository.existsByProductType_Id(productTypeId);
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findByIdWithDetails(productId)
                .map(ProductEntity::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAllWithDetails().stream()
                .map(ProductEntity::toDomain)
                .toList();
    }

    @Override
    public Product save(Product product) {
        CategoryEntity categoryRef = categoryJpaRepository.getReferenceById(
                product.getCategory().getCategoryId());
        ProductTypeEntity productTypeRef = productTypeJpaRepository.getReferenceById(
                product.getProductType().getProductTypeId());
        ProductEntity entity = ProductEntity.fromDomain(product, categoryRef, productTypeRef);
        return productJpaRepository.save(entity).toDomain();
    }

    @Override
    public void delete(Product product) {
        productJpaRepository.deleteById(product.getProductId());
    }
}
