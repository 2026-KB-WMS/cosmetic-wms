package com.kb.cosmetic_wms.product.adapter.in.web;

import com.kb.cosmetic_wms.product.application.port.in.RegisterProductCommand;
import com.kb.cosmetic_wms.product.domain.constants.ProductConstants;
import com.kb.cosmetic_wms.product.domain.constants.ProductInfoConstants;
import com.kb.cosmetic_wms.product.domain.enums.TemperatureType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record RegisterProductRequest(

        @NotBlank(message = ProductConstants.BRAND_NAME_REQUIRED_MESSAGE)
        @Size(max = ProductConstants.BRAND_NAME_MAX_LENGTH,
                message = ProductConstants.INVALID_BRAND_NAME_LENGTH_MESSAGE)
        String brandName,

        @NotBlank(message = ProductConstants.PRODUCT_NAME_REQUIRED_MESSAGE)
        @Size(max = ProductConstants.PRODUCT_NAME_MAX_LENGTH,
                message = ProductConstants.INVALID_PRODUCT_NAME_LENGTH_MESSAGE)
        String productName,

        @Min(value = ProductConstants.MIN_PRICE_BOUND,
                message = ProductConstants.INVALID_PRODUCT_PRICE_MESSAGE)
        int productPrice,

        @NotNull(message = ProductConstants.TEMPERATURE_TYPE_REQUIRED_MESSAGE)
        TemperatureType temperatureType,

        @NotNull(message = ProductConstants.CATEGORY_ID_REQUIRED_MESSAGE)
        Long categoryId,

        @NotNull(message = ProductConstants.PRODUCT_TYPE_ID_REQUIRED_MESSAGE)
        Long productTypeId,

        @Valid
        @NotNull(message = ProductConstants.PRODUCT_INFO_REQUEST_REQUIRED_MESSAGE)
        ProductInfoRequest productInfo

) {

    public record ProductInfoRequest(

            @Size(max = ProductInfoConstants.SKIN_TYPE_MAX_LENGTH,
                    message = ProductInfoConstants.INVALID_SKIN_TYPE_LENGTH_MESSAGE)
            String skinType,

            @Size(max = ProductInfoConstants.FUNCTION_TYPE_MAX_LENGTH,
                    message = ProductInfoConstants.INVALID_FUNCTION_TYPE_LENGTH_MESSAGE)
            String functionType,

            @Valid
            @NotNull(message = ProductInfoConstants.VOLUME_REQUEST_REQUIRED_MESSAGE)
            VolumeRequest volume,

            String ingredients,

            String cautions,

            @Size(max = ProductInfoConstants.STORAGE_CONDITION_MAX_LENGTH,
                    message = ProductInfoConstants.INVALID_STORAGE_CONDITION_LENGTH_MESSAGE)
            String storageCondition
    ) {
    }

    public record VolumeRequest(

            @Positive(message = ProductInfoConstants.INVALID_VOLUME_MIN_MESSAGE)
            @Max(value = ProductInfoConstants.MAX_VOLUME,
                    message = ProductInfoConstants.INVALID_VOLUME_MAX_MESSAGE)
            int value,

            @NotBlank(message = ProductInfoConstants.UNIT_REQUIRED_MESSAGE)
            @Pattern(regexp = ProductInfoConstants.ALLOWED_UNITS_REGEX,
                    message = ProductInfoConstants.INVALID_UNIT_FORMAT_MESSAGE)
            String unit
    ) {
    }

    public RegisterProductCommand toCommand() {
        return new RegisterProductCommand(
                brandName, productName, productPrice, temperatureType,
                categoryId, productTypeId,
                productInfo.skinType(), productInfo.functionType(),
                productInfo.volume().value(), productInfo.volume().unit(),
                productInfo.ingredients(), productInfo.cautions(), productInfo.storageCondition()
        );
    }
}