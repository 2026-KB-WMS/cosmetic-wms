package com.kb.cosmetic_wms.product.product.adapter.in.web;

import com.kb.cosmetic_wms.product.product.application.port.in.*;
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

    private final RegisterProductUseCase registerProductUseCase;
    private final FindProductUseCase findProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    @GetMapping
    public ResponseEntity<List<ProductSummaryResponse>> getProducts() {
        List<ProductSummaryResponse> response = findProductUseCase.findAll().stream()
                .map(ProductSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(ProductDetailResponse.from(findProductUseCase.findById(productId)));
    }

    @PostMapping
    public ResponseEntity<ProductDetailResponse> register(
            @Valid @RequestBody RegisterProductRequest request) {
        ProductResult result = registerProductUseCase.register(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductDetailResponse.from(result));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResult result = updateProductUseCase.update(productId, request.toCommand());
        return ResponseEntity.ok(ProductDetailResponse.from(result));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        deleteProductUseCase.delete(productId);
        return ResponseEntity.noContent().build();
    }
}