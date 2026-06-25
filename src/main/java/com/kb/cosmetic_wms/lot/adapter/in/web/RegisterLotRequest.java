package com.kb.cosmetic_wms.lot.adapter.in.web;

import com.kb.cosmetic_wms.lot.application.port.in.RegisterLotCommand;
import com.kb.cosmetic_wms.lot.domain.constants.LotConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RegisterLotRequest(

        @NotBlank(message = LotConstants.LOT_NO_REQUIRED_MESSAGE)
        @Size(max = LotConstants.LOT_NUMBER_MAX_LENGTH,
                message = LotConstants.INVALID_LOT_NUMBER_LENGTH_MESSAGE)
        @Pattern(regexp = LotConstants.LOT_NO_PATTERN_REGEX,
                message = LotConstants.INVALID_LOT_NO_FORMAT_MESSAGE)
        String lotNumber,

        @NotNull(message = LotConstants.MANUFACTURING_DATE_REQUIRED_MESSAGE)
        LocalDateTime manufacturingDate,

        @NotNull(message = LotConstants.EXPIRATION_DATE_REQUIRED_MESSAGE)
        LocalDateTime expirationDate,

        @NotNull(message = LotConstants.PRODUCT_REQUIRED_MESSAGE)
        Long productId

) {
    public RegisterLotCommand toCommand() {
        return new RegisterLotCommand(lotNumber, manufacturingDate, expirationDate, productId);
    }
}