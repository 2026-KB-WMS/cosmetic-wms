package com.kb.ordering.product.adapter.in.web;

import com.kb.ordering.product.adapter.in.web.dto.CategoryResponse;
import com.kb.ordering.product.adapter.in.web.dto.RegisterCategoryRequest;
import com.kb.ordering.product.application.port.in.DeleteCategoryUseCase;
import com.kb.ordering.product.application.port.in.FindCategoryUseCase;
import com.kb.ordering.product.application.port.in.RegisterCategoryUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final RegisterCategoryUseCase registerCategoryUseCase;
    private final FindCategoryUseCase findCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        List<CategoryResponse> response = findCategoryUseCase.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable("categoryId") Long categoryId) {
        return ResponseEntity.ok(CategoryResponse.from(findCategoryUseCase.findById(categoryId)));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> register(@Valid @RequestBody RegisterCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryResponse.from(registerCategoryUseCase.register(request.toCommand())));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable("categoryId") Long categoryId) {
        deleteCategoryUseCase.delete(categoryId);
        return ResponseEntity.noContent().build();
    }
}