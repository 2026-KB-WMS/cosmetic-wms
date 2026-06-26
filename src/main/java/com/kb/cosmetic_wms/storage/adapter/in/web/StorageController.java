package com.kb.cosmetic_wms.storage.adapter.in.web;

import com.kb.cosmetic_wms.storage.application.port.in.AddSectionUseCase;
import com.kb.cosmetic_wms.storage.application.port.in.FindWarehouseUseCase;
import com.kb.cosmetic_wms.storage.application.port.in.RegisterWarehouseUseCase;
import com.kb.cosmetic_wms.storage.application.port.in.WarehouseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/storages")
@RequiredArgsConstructor
public class StorageController {

    private final RegisterWarehouseUseCase registerWarehouseUseCase;
    private final AddSectionUseCase addSectionUseCase;
    private final FindWarehouseUseCase findWarehouseUseCase;

    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponse> registerWarehouse(
            @RequestBody @Valid WarehouseCreateRequest request
    ) {
        WarehouseResult result = registerWarehouseUseCase.register(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(WarehouseResponse.from(result));
    }

    @PostMapping("/warehouses/{warehouseId}/sections")
    public ResponseEntity<WarehouseResponse> addSectionToWarehouse(
            @PathVariable Long warehouseId,
            @RequestBody @Valid SectionCreateRequest request
    ) {
        WarehouseResult result = addSectionUseCase.addSection(warehouseId, request.toCommand());
        return ResponseEntity.ok(WarehouseResponse.from(result));
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
        List<WarehouseResponse> responses = findWarehouseUseCase.findAll().stream()
                .map(WarehouseResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable Long warehouseId) {
        WarehouseResult result = findWarehouseUseCase.findById(warehouseId);
        return ResponseEntity.ok(WarehouseResponse.from(result));
    }
}