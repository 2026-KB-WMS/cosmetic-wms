package com.kb.cosmetic_wms.domain.storage.service;

import com.kb.cosmetic_wms.domain.storage.dto.SectionCreateRequestDto;
import com.kb.cosmetic_wms.domain.storage.dto.WarehouseCreateRequestDto;
import com.kb.cosmetic_wms.domain.storage.dto.WarehouseResponseDto;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.exception.DuplicateWarehouseException;
import com.kb.cosmetic_wms.domain.storage.exception.WarehouseNotFoundException;
import com.kb.cosmetic_wms.domain.storage.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StorageService {

    private final WarehouseRepository warehouseRepository;

    /**
     * 신규 물류 창고(Warehouse) 마스터 데이터를 시스템에 등록
     *
     * @param requestDto 창고 생성 요청 정보 (창고명, 주소, 설정 온도 범위, 총 수용량)
     * @return 등록이 완료된 창고의 상세 및 공백 섹션 리스트 구조를 포함한 응답 DTO
     */
    @Transactional
    public WarehouseResponseDto registerWarehouse(WarehouseCreateRequestDto requestDto) {
        if (warehouseRepository.existsByWarehouseNameAndAddress(requestDto.warehouseName(), requestDto.address())) {
            throw new DuplicateWarehouseException();
        }

        Warehouse warehouse = Warehouse.create(
                requestDto.warehouseName(),
                requestDto.address(),
                requestDto.targetTemp(),
                requestDto.capacity()
        );

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return WarehouseResponseDto.from(savedWarehouse);
    }

    /**
     * 특정 창고 마스터 데이터 하위에 세부 보관 구역(Section)을 추가
     *
     * @param warehouseId 구역을 추가할 대상 창고 고유 ID
     * @param requestDto  추가할 구역 정보 (타입, 코드, 구역명, 설정 온도, 최대 수용 용량)
     * @return 구역이 추가된 후 최종 창고 마스터 정보 응답 DTO
     */
    @Transactional
    public WarehouseResponseDto addSectionToWarehouse(Long warehouseId, SectionCreateRequestDto requestDto) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(WarehouseNotFoundException::new);

        switch (requestDto.sectionType()) {
            case DOCKING -> warehouse.addDockingSection(
                    requestDto.sectionCode(),
                    requestDto.sectionName(),
                    requestDto.temperatureType(),
                    requestDto.maxCapacity()
            );
            case QUARANTINE -> warehouse.addQuarantineSection(
                    requestDto.sectionCode(),
                    requestDto.sectionName(),
                    requestDto.maxCapacity()
            );
            default -> warehouse.addStorageSection(
                    requestDto.sectionCode(),
                    requestDto.sectionName(),
                    requestDto.sectionType(),
                    requestDto.temperatureType(),
                    requestDto.maxCapacity()
            );
        }
        return WarehouseResponseDto.from(warehouse);
    }

    public List<WarehouseResponseDto> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(WarehouseResponseDto::from)
                .toList();
    }

    public WarehouseResponseDto getWarehouseById(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(WarehouseNotFoundException::new);
        return WarehouseResponseDto.from(warehouse);
    }
}
