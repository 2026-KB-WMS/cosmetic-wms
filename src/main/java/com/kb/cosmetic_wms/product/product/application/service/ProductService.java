package com.kb.cosmetic_wms.product.product.application.service;

import com.kb.cosmetic_wms.product.product.application.port.in.*;
import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.category.application.port.out.CategoryPort;
import com.kb.cosmetic_wms.product.category.domain.exception.CategoryNotFoundException;
import com.kb.cosmetic_wms.product.category.domain.model.Category;
import com.kb.cosmetic_wms.product.product.domain.exception.DuplicateProductException;
import com.kb.cosmetic_wms.product.product.domain.exception.ProductNotFoundException;
import com.kb.cosmetic_wms.product.product.domain.model.Product;
import com.kb.cosmetic_wms.product.product.domain.model.ProductInfo;
import com.kb.cosmetic_wms.product.product.domain.valueobject.Volume;
import com.kb.cosmetic_wms.product.producttype.application.port.out.ProductTypePort;
import com.kb.cosmetic_wms.product.producttype.domain.exception.ProductTypeNotFoundException;
import com.kb.cosmetic_wms.product.producttype.domain.model.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService implements RegisterProductUseCase, FindProductUseCase,
        UpdateProductUseCase, DeleteProductUseCase {

    private final ProductPort productPort;
    private final CategoryPort categoryPort;
    private final ProductTypePort productTypePort;

    @Override
    @Transactional
    public ProductResult register(RegisterProductCommand command) {
        Category category = categoryPort.findById(command.categoryId())
                .orElseThrow(CategoryNotFoundException::new);
        ProductType productType = productTypePort.findById(command.productTypeId())
                .orElseThrow(ProductTypeNotFoundException::new);

        if (productPort.existsDuplicateProduct(
                command.brandName(), command.productName(),
                category.getCategoryId(), productType.getProductTypeId(),
                command.volumeValue(), command.volumeUnit())) {
            throw new DuplicateProductException();
        }

        Volume volume = Volume.of(command.volumeValue(), command.volumeUnit());
        ProductInfo productInfo = ProductInfo.create(
                command.skinType(), command.functionType(), volume,
                command.ingredients(), command.cautions(), command.storageCondition()
        );
        Product product = Product.create(
                command.brandName(), command.productName(), command.productPrice(),
                command.temperatureType(), category, productType, productInfo
        );

        Product savedProduct = productPort.save(product);
        savedProduct.assignSkuCode(savedProduct.getProductId());
        return ProductResult.from(productPort.save(savedProduct));
    }

    @Override
    public List<ProductSummaryResult> findAll() {
        return productPort.findAll().stream()
                .map(ProductSummaryResult::from)
                .toList();
    }

    @Override
    public ProductResult findById(Long productId) {
        Product product = productPort.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        return ProductResult.from(product);
    }

    @Override
    public boolean existsById(Long productId) {
        return productPort.findById(productId).isPresent();
    }

    @Override
    public boolean allExistByIds(Collection<Long> productIds) {
        return productPort.allExistByIds(productIds);
    }

    @Override
    @Transactional
    public ProductResult update(Long productId, UpdateProductCommand command) {
        Product product = productPort.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        Volume existingVolume = product.getProductInfo().getVolume();
        ProductInfo updatedInfo = ProductInfo.create(
                command.skinType(), command.functionType(), existingVolume,
                command.ingredients(), command.cautions(), command.storageCondition()
        );

        product.update(command.productName(), command.productPrice(),
                command.temperatureType(), updatedInfo);

        return ProductResult.from(productPort.save(product));
    }

    @Override
    @Transactional
    public void delete(Long productId) {
        Product product = productPort.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        productPort.delete(product);
    }
}