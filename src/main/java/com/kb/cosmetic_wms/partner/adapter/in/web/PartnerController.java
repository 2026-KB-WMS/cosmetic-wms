package com.kb.cosmetic_wms.partner.adapter.in.web;

import com.kb.cosmetic_wms.partner.application.port.in.FindPartnerUseCase;
import com.kb.cosmetic_wms.partner.application.port.in.PartnerResult;
import com.kb.cosmetic_wms.partner.application.port.in.RegisterPartnerUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final RegisterPartnerUseCase registerPartnerUseCase;
    private final FindPartnerUseCase findPartnerUseCase;

    @PostMapping
    public ResponseEntity<PartnerResponse> register(@Valid @RequestBody RegisterPartnerRequest request) {
        PartnerResult result = registerPartnerUseCase.register(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(PartnerResponse.from(result));
    }

    @GetMapping("/{partnerId}")
    public ResponseEntity<PartnerResponse> findById(@PathVariable("partnerId") Long partnerId) {
        PartnerResult result = findPartnerUseCase.findById(partnerId);
        return ResponseEntity.ok(PartnerResponse.from(result));
    }
}