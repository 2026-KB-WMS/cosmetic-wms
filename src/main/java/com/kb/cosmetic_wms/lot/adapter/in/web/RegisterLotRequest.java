package com.kb.cosmetic_wms.lot.adapter.in.web;

import com.kb.cosmetic_wms.lot.application.port.in.RegisterLotCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RegisterLotRequest(

        @NotBlank(message = "로트 번호는 필수 입력 값입니다.")
        @Size(max = 50, message = "로트 번호는 최대 50자까지 입력 가능합니다.")
        @Pattern(regexp = "^[A-Z]{3}-\\d{6}-[A-Z0-9]{2}-\\d{4}$",
                message = "올바르지 않은 로트 번호 형식입니다. (규격: [카테고리3자]-[YYMMDD]-[공장2자]-[일련번호4자])")
        String lotNumber,

        @NotNull(message = "제조일자는 필수 입력 값입니다.")
        LocalDateTime manufacturingDate,

        @NotNull(message = "유통기한은 필수 입력 값입니다.")
        LocalDateTime expirationDate,

        @NotNull(message = "상품 식별자(ID)는 필수입니다.")
        Long productId

) {
    public RegisterLotCommand toCommand() {
        return new RegisterLotCommand(lotNumber, manufacturingDate, expirationDate, productId);
    }
}
