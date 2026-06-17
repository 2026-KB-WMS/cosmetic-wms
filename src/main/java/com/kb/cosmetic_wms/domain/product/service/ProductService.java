package com.kb.cosmetic_wms.domain.product.service;

import com.kb.cosmetic_wms.domain.product.constants.ProductConstants;
import com.kb.cosmetic_wms.domain.product.dto.ProductCreateRequestDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductDetailResponseDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductSummaryResponseDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductUpdateRequestDto;
import com.kb.cosmetic_wms.domain.product.entity.*;
import com.kb.cosmetic_wms.domain.product.exception.*;
import com.kb.cosmetic_wms.domain.product.repository.CategoryRepository;
import com.kb.cosmetic_wms.domain.product.repository.ProductRepository;
import com.kb.cosmetic_wms.domain.product.repository.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductTypeRepository productTypeRepository;

    @Transactional
    public ProductDetailResponseDto register(ProductCreateRequestDto request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(CategoryNotFoundException::new);
        ProductType productType = productTypeRepository.findById(request.productTypeId())
                .orElseThrow(ProductTypeNotFoundException::new);

        int volumeValue = request.productInfo().volume().value();
        String volumeUnit = request.productInfo().volume().unit();

        if (productRepository.existsDuplicateProduct(
                request.brandName(), request.productName(),
                category, productType, volumeValue, volumeUnit)) {
            throw new DuplicateProductException();
        }

        int nextSequence = productRepository.findNextSequence(
                category.getCategoryCode(), productType.getTypeCode(),
                volumeValue, request.brandName());
        if (nextSequence > ProductConstants.SEQUENCE_MAX_BOUND) {
            throw new SkuSequenceOverflowException();
        }

        ProductInfo productInfo = toProductInfo(request.productInfo());
        Product product = Product.create(
                request.brandName(),
                request.productName(),
                request.productPrice(),
                request.temperatureType(),
                category,
                productType,
                productInfo,
                nextSequence
        );

        return ProductDetailResponseDto.from(productRepository.save(product));
    }

    public List<ProductSummaryResponseDto> getProducts() {
        return productRepository.findAll().stream()
                .map(ProductSummaryResponseDto::from)
                .toList();
    }

    public ProductDetailResponseDto getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        return ProductDetailResponseDto.from(product);
    }

    @Transactional
    public ProductDetailResponseDto updateProduct(Long productId, ProductUpdateRequestDto request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        // SKU 구성 요소인 volume은 기존 값을 그대로 유지
        Volume existingVolume = product.getProductInfo().getVolume();
        ProductUpdateRequestDto.ProductInfoUpdateRequest infoRequest = request.productInfo();
        ProductInfo updatedInfo = ProductInfo.create(
                infoRequest.skinType(),
                infoRequest.functionType(),
                existingVolume,
                infoRequest.ingredients(),
                infoRequest.cautions(),
                infoRequest.storageCondition()
        );

        product.update(request.productName(), request.productPrice(),
                request.temperatureType(), updatedInfo);

        return ProductDetailResponseDto.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        productRepository.delete(product);
    }

    private ProductInfo toProductInfo(ProductCreateRequestDto.ProductInfoRequest dto) {
        Volume volume = Volume.of(dto.volume().value(), dto.volume().unit());
        return ProductInfo.create(
                dto.skinType(),
                dto.functionType(),
                volume,
                dto.ingredients(),
                dto.cautions(),
                dto.storageCondition()
        );
    }
}
