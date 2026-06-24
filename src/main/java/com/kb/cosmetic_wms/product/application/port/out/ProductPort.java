package com.kb.cosmetic_wms.product.application.port.out;

import com.kb.cosmetic_wms.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductPort {
    boolean existsDuplicateProduct(String brandName, String productName,
                                   Long categoryId, Long productTypeId,
                                   int volumeValue, String volumeUnit);
    Optional<Product> findById(Long productId);
    List<Product> findAll();
    Product save(Product product);
    void delete(Product product);
}