package com.kb.cosmetic_wms.domain.partner.controller;

import com.kb.cosmetic_wms.domain.partner.dto.PartnerCreateRequestDto;
import com.kb.cosmetic_wms.domain.partner.dto.PartnerResponseDto;
import com.kb.cosmetic_wms.domain.partner.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    /**
     * 신규 협력사 파트너 정보를 데이터에 등록
     *
     * @param requestDto 협력사 등록 요청 DTO (파트너명, 타입, 사업자번호 필수)
     * @return 생성된 협력사의 상세 정보를 포함한 ResponseEntity
     * @throws com.kb.cosmetic_wms.domain.partner.exception.DuplicatePartnerException 이미 동일한 사업자 번호의 협력사가 존재할 경우 발생
     */
    @PostMapping
    public ResponseEntity<PartnerResponseDto> register(@Valid @RequestBody PartnerCreateRequestDto requestDto) {
        PartnerResponseDto responseDto = partnerService.register(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    /**
     * 협력사 고유 식별 번호(ID)를 기준으로 단건 조회합니다.
     *
     * @param partnerId 조회하고자 하는 협력사 고유 식별 번호 (PK)
     * @return 조회된 협력사 상세 정보를 포함한 ResponseEntity
     * @throws com.kb.cosmetic_wms.domain.partner.exception.PartnerNotFoundException 지정한 협력사 ID에 해당하는 데이터가 데이터베이스에 존재하지 않을 경우 발생
     */
    @GetMapping("/{partnerId}")
    public ResponseEntity<PartnerResponseDto> findById(@PathVariable("partnerId") Long partnerId) {
        PartnerResponseDto responseDto = partnerService.findById(partnerId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }
}
