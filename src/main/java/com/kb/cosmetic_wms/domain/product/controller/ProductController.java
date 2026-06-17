package com.kb.cosmetic_wms.domain.product.controller;

import com.kb.cosmetic_wms.domain.product.dto.ProductCreateRequestDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductDetailResponseDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductSummaryResponseDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductUpdateRequestDto;
import com.kb.cosmetic_wms.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductSummaryResponseDto>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponseDto> getProduct(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @PostMapping
    public ResponseEntity<ProductDetailResponseDto> register(
            @Valid @RequestBody ProductCreateRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.register(request));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDetailResponseDto> updateProduct(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductUpdateRequestDto request) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
