package com.kb.cosmetic_wms.putaway.adapter.in.web;

import com.kb.cosmetic_wms.putaway.application.port.in.CompletePutawayUseCase;
import com.kb.cosmetic_wms.putaway.application.port.in.FindPutawayOrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/putaway-orders")
@RequiredArgsConstructor
public class PutawayOrderController {

    private final FindPutawayOrderUseCase findPutawayOrderUseCase;
    private final CompletePutawayUseCase completePutawayUseCase;

    @GetMapping("/{putawayOrderId}")
    public ResponseEntity<PutawayOrderDetailResponse> getPutawayOrder(@PathVariable Long putawayOrderId) {
        return ResponseEntity.ok(PutawayOrderDetailResponse.from(findPutawayOrderUseCase.findById(putawayOrderId)));
    }

    @PatchMapping("/{putawayOrderId}/complete")
    public ResponseEntity<PutawayOrderDetailResponse> completePutaway(@PathVariable Long putawayOrderId) {
        return ResponseEntity.ok(PutawayOrderDetailResponse.from(completePutawayUseCase.complete(putawayOrderId)));
    }
}
