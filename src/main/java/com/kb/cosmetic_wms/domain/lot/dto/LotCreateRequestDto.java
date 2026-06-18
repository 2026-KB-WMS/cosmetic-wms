package com.kb.cosmetic_wms.domain.lot.dto;

import com.kb.cosmetic_wms.domain.lot.constants.LotConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record LotCreateRequestDto(

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
}
