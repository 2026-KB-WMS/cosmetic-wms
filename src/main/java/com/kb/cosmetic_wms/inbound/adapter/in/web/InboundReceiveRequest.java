package com.kb.cosmetic_wms.inbound.adapter.in.web;

import com.kb.cosmetic_wms.inbound.application.port.in.ReceiveInboundCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.List;

public record InboundReceiveRequest(
        @NotEmpty(message = "수령 확인할 품목 라인이 최소 하나 이상 필요합니다.") @Valid List<LineItem> lines
) {
    public record LineItem(
            @NotNull(message = "라인 ID는 필수입니다.") Long lineId,
            @PositiveOrZero(message = "수령 수량은 0 이상이어야 합니다.") int receivedQuantity,
            @NotBlank(message = "제조사 로트 번호는 필수입니다.") String manufacturerLotNumber,
            @NotNull(message = "제조일자는 필수입니다.") LocalDateTime manufacturingDate,
            @NotNull(message = "유통기한은 필수입니다.") LocalDateTime expirationDate
    ) {}

    public ReceiveInboundCommand toCommand() {
        List<ReceiveInboundCommand.LineItem> lineItems = lines.stream()
                .map(l -> new ReceiveInboundCommand.LineItem(
                        l.lineId(), l.receivedQuantity(),
                        l.manufacturerLotNumber(), l.manufacturingDate(), l.expirationDate()))
                .toList();
        return new ReceiveInboundCommand(lineItems);
    }
}
