package com.kb.ordering.store.adapter.in.web;

import com.kb.ordering.store.adapter.in.web.dto.RegisterStoreRequest;
import com.kb.ordering.store.adapter.in.web.dto.StoreResponse;
import com.kb.ordering.store.application.port.in.FindStoreUseCase;
import com.kb.ordering.store.application.port.in.RegisterStoreUseCase;
import com.kb.ordering.store.application.port.in.dto.StoreResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final RegisterStoreUseCase registerStoreUseCase;
    private final FindStoreUseCase findStoreUseCase;

    @PostMapping
    public ResponseEntity<StoreResponse> register(@Valid @RequestBody RegisterStoreRequest request) {
        StoreResult result = registerStoreUseCase.register(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(StoreResponse.from(result));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> findById(@PathVariable("storeId") Long storeId) {
        StoreResult result = findStoreUseCase.findById(storeId);
        return ResponseEntity.ok(StoreResponse.from(result));
    }
}