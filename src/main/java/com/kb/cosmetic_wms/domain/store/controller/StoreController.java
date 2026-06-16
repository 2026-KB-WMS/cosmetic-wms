package com.kb.cosmetic_wms.domain.store.controller;

import com.kb.cosmetic_wms.domain.store.dto.StoreCreateRequestDto;
import com.kb.cosmetic_wms.domain.store.dto.StoreResponseDto;
import com.kb.cosmetic_wms.domain.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * 신규 가맹점(점포)을 마스터 데이터에 등록
     *
     * @param requestDto 가맹점 등록을 위한 요청 정보 DTO (점포명, 주소 필수)
     * @return 생성된 가맹점의 식별 정보 및 상세 데이터를 포함한 ResponseEntity
     * @throws com.kb.cosmetic_wms.domain.store.exception.DuplicateStoreException 이미 동일한 점포명과 주소의 가맹점이 시스템에 존재할 경우 발생
     */
    @PostMapping
    public ResponseEntity<StoreResponseDto> register(@Valid @RequestBody StoreCreateRequestDto requestDto) {
        StoreResponseDto responseDto = storeService.register(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    /**
     * 가맹점 고유 식별 번호(ID)를 기준으로 단건 조회
     *
     * @param storeId 조회하고자 하는 가맹점 고유 식별 번호 (PK)
     * @return 조회된 가맹점 상세 정보를 포함한 ResponseEntity
     * @throws com.kb.cosmetic_wms.domain.store.exception.StoreNotFoundException 지정한 가맹점 ID에 해당하는 데이터가 데이터베이스에 존재하지 않을 경우 발생
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponseDto> findById(@PathVariable("storeId") Long storeId) {
        StoreResponseDto responseDto = storeService.findById(storeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }
}
