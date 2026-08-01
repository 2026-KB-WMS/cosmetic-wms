package com.kb.ordering.product.application.port.out;

import com.kb.ordering.product.domain.product.enums.TemperatureType;
import com.kb.ordering.product.domain.product.model.Product;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductPort {
    boolean existsDuplicateProduct(String brandName, String productName,
                                   Long categoryId, Long productTypeId,
                                   int volumeValue, String volumeUnit);

    boolean existsById(Long productId);

    boolean allExistByIds(Collection<Long> productIds);

    Map<Long, TemperatureType> findTemperatureTypesByIds(Collection<Long> productIds);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByProductTypeId(Long productTypeId);

    Optional<Product> findById(Long productId);

    List<Product> findAll();

    Product save(Product product);

    void delete(Product product);
}