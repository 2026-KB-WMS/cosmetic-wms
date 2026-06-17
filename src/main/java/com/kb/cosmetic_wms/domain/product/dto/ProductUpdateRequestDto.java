package com.kb.cosmetic_wms.domain.product.dto;

import com.kb.cosmetic_wms.domain.product.constants.ProductConstants;
import com.kb.cosmetic_wms.domain.product.constants.ProductInfoConstants;
import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequestDto(

        @NotBlank(message = ProductConstants.PRODUCT_NAME_REQUIRED_MESSAGE)
        @Size(max = ProductConstants.PRODUCT_NAME_MAX_LENGTH,
                message = ProductConstants.INVALID_PRODUCT_NAME_LENGTH_MESSAGE)
        String productName,

        @Min(value = ProductConstants.MIN_PRICE_BOUND,
                message = ProductConstants.INVALID_PRODUCT_PRICE_MESSAGE)
        int productPrice,

        @NotNull(message = ProductConstants.TEMPERATURE_TYPE_REQUIRED_MESSAGE)
        TemperatureType temperatureType,

        @Valid
        @NotNull(message = ProductConstants.PRODUCT_INFO_REQUEST_REQUIRED_MESSAGE)
        ProductInfoUpdateRequest productInfo

) {
    public record ProductInfoUpdateRequest(

            @Size(max = ProductInfoConstants.SKIN_TYPE_MAX_LENGTH,
                    message = ProductInfoConstants.INVALID_SKIN_TYPE_LENGTH_MESSAGE)
            String skinType,

            @Size(max = ProductInfoConstants.FUNCTION_TYPE_MAX_LENGTH,
                    message = ProductInfoConstants.INVALID_FUNCTION_TYPE_LENGTH_MESSAGE)
            String functionType,

            String ingredients,

            String cautions,

            @Size(max = ProductInfoConstants.STORAGE_CONDITION_MAX_LENGTH,
                    message = ProductInfoConstants.INVALID_STORAGE_CONDITION_LENGTH_MESSAGE)
            String storageCondition

    ) {}
}
