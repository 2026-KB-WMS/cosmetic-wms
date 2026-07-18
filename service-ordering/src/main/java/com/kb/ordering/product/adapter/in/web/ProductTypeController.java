package com.kb.ordering.product.adapter.in.web;

import com.kb.ordering.product.adapter.in.web.dto.ProductTypeResponse;
import com.kb.ordering.product.adapter.in.web.dto.RegisterProductTypeRequest;
import com.kb.ordering.product.application.port.in.DeleteProductTypeUseCase;
import com.kb.ordering.product.application.port.in.FindProductTypeUseCase;
import com.kb.ordering.product.application.port.in.RegisterProductTypeUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-types")
@RequiredArgsConstructor
public class ProductTypeController {

    private final RegisterProductTypeUseCase registerProductTypeUseCase;
    private final FindProductTypeUseCase findProductTypeUseCase;
    private final DeleteProductTypeUseCase deleteProductTypeUseCase;

    @GetMapping
    public ResponseEntity<List<ProductTypeResponse>> getProductTypes() {
        List<ProductTypeResponse> response = findProductTypeUseCase.findAll().stream()
                .map(ProductTypeResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productTypeId}")
    public ResponseEntity<ProductTypeResponse> getProductType(@PathVariable("productTypeId") Long productTypeId) {
        return ResponseEntity.ok(ProductTypeResponse.from(findProductTypeUseCase.findById(productTypeId)));
    }

    @PostMapping
    public ResponseEntity<ProductTypeResponse> register(@Valid @RequestBody RegisterProductTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductTypeResponse.from(registerProductTypeUseCase.register(request.toCommand())));
    }

    @DeleteMapping("/{productTypeId}")
    public ResponseEntity<Void> delete(@PathVariable("productTypeId") Long productTypeId) {
        deleteProductTypeUseCase.delete(productTypeId);
        return ResponseEntity.noContent().build();
    }
}
