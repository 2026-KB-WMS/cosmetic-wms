package com.kb.cosmetic_wms.domain.storage.controller;

import com.kb.cosmetic_wms.domain.storage.dto.SectionCreateRequestDto;
import com.kb.cosmetic_wms.domain.storage.dto.WarehouseCreateRequestDto;
import com.kb.cosmetic_wms.domain.storage.dto.WarehouseResponseDto;
import com.kb.cosmetic_wms.domain.storage.service.StorageService;
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

    private final StorageService storageService;

    /**
     * 신규 창고 등록
     */
    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponseDto> registerWarehouse(
            @RequestBody @Valid WarehouseCreateRequestDto requestDto
    ) {
        WarehouseResponseDto response = storageService.registerWarehouse(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 창고에 섹션 추가
     */
    @PostMapping("/warehouses/{warehouseId}/sections")
    public ResponseEntity<WarehouseResponseDto> addSectionToWarehouse(
            @PathVariable Long warehouseId,
            @RequestBody @Valid SectionCreateRequestDto requestDto
    ) {
        WarehouseResponseDto response = storageService.addSectionToWarehouse(warehouseId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 창고 조회
     *
     * @return 창고 목록
     */
    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDto>> getAllWarehouses() {
        List<WarehouseResponseDto> responses = storageService.getAllWarehouses();
        return ResponseEntity.ok(responses);
    }

    /**
     * 창고 상세 조회
     *
     * @param warehouseId 창고 ID
     * @return 창고 정보
     * @throws WarehouseNotFoundException 해당 창고가 존재하지 않는 경우
     */
    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<WarehouseResponseDto> getWarehouseById(@PathVariable Long warehouseId) {
        WarehouseResponseDto response = storageService.getWarehouseById(warehouseId);
        return ResponseEntity.ok(response);
    }
}
