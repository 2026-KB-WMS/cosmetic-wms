package com.kb.cosmetic_wms.lot.adapter.in.web;

import com.kb.cosmetic_wms.lot.application.port.in.RegisterLotCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RegisterLotRequest(

        @NotNull(message = "입고 ID는 필수 입력 값입니다.")
        @Positive(message = "입고 ID는 양수여야 합니다.")
        Long inboundId,

        @NotBlank(message = "제조사 로트 번호는 필수 입력 값입니다.")
        @Size(max = 20, message = "제조사 로트 번호는 최대 20자까지 입력 가능합니다.")
        @Pattern(regexp = "^[A-Z0-9][A-Z0-9\\-]{0,19}$",
                message = "올바르지 않은 제조사 로트 번호 형식입니다. (영문 대문자, 숫자, 하이픈만 허용, 최대 20자)")
        String manufacturerLotNumber,

        @NotNull(message = "제조일자는 필수 입력 값입니다.")
        LocalDateTime manufacturingDate,

        @NotNull(message = "유통기한은 필수 입력 값입니다.")
        LocalDateTime expirationDate,

        @NotNull(message = "상품 식별자(ID)는 필수입니다.")
        Long productId

) {
    public RegisterLotCommand toCommand() {
        return new RegisterLotCommand(inboundId, manufacturerLotNumber,
                manufacturingDate, expirationDate, productId);
    }
}
