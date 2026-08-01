package com.kb.ordering.product.application.service;

import com.kb.ordering.product.application.port.in.DeleteProductUseCase;
import com.kb.ordering.product.application.port.in.FindProductUseCase;
import com.kb.ordering.product.application.port.in.RegisterProductUseCase;
import com.kb.ordering.product.application.port.in.UpdateProductUseCase;
import com.kb.ordering.product.application.port.in.dto.ProductResult;
import com.kb.ordering.product.application.port.in.dto.ProductSummaryResult;
import com.kb.ordering.product.application.port.in.dto.RegisterProductCommand;
import com.kb.ordering.product.application.port.in.dto.UpdateProductCommand;
import com.kb.ordering.product.application.port.out.CategoryPort;
import com.kb.ordering.product.application.port.out.ProductPort;
import com.kb.ordering.product.application.port.out.ProductTypePort;
import com.kb.ordering.product.domain.category.exception.CategoryNotFoundException;
import com.kb.ordering.product.domain.product.exception.DuplicateProductException;
import com.kb.ordering.product.domain.product.exception.ProductNotFoundException;
import com.kb.ordering.product.domain.product.model.Product;
import com.kb.ordering.product.domain.product.model.ProductInfo;
import com.kb.ordering.product.domain.product.valueobject.Volume;
import com.kb.ordering.product.domain.producttype.exception.ProductTypeNotFoundException;
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
        categoryPort.findById(command.categoryId()).orElseThrow(CategoryNotFoundException::new);
        productTypePort.findById(command.productTypeId()).orElseThrow(ProductTypeNotFoundException::new);

        if (productPort.existsDuplicateProduct(
                command.brandName(), command.productName(),
                command.categoryId(), command.productTypeId(),
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
                command.temperatureType(), command.categoryId(), command.productTypeId(), productInfo
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
        return productPort.existsById(productId);
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