package com.kb.cosmetic_wms.inbound.adapter.in.web;

import com.kb.cosmetic_wms.inbound.application.port.in.RegisterInboundCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

public record InboundCreateRequest(
        @NotNull(message = "입고 창고 정보는 필수입니다.") Long warehouseId,
        @NotNull(message = "입고 파트너 정보는 필수입니다.") Long partnerId,
        @NotNull(message = "입고 예정일은 필수입니다.") LocalDateTime inboundDate,
        @NotEmpty(message = "입고 품목 라인이 최소 하나 이상 필요합니다.") @Valid List<LineRequest> lines
) {
    public record LineRequest(
            @NotNull(message = "입고 상품 정보는 필수입니다.") Long productId,
            @Positive(message = "입고 예정 수량은 0보다 커야 합니다.") int orderedQuantity
    ) {}

    public RegisterInboundCommand toCommand() {
        List<RegisterInboundCommand.LineItem> lineItems = lines.stream()
                .map(l -> new RegisterInboundCommand.LineItem(l.productId(), l.orderedQuantity()))
                .toList();
        return new RegisterInboundCommand(warehouseId, partnerId, inboundDate, lineItems);
    }
}
