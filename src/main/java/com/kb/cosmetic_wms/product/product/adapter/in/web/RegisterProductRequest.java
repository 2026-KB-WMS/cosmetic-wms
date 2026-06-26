package com.kb.cosmetic_wms.product.product.adapter.in.web;

import com.kb.cosmetic_wms.product.product.application.port.in.RegisterProductCommand;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.product.product.domain.model.Product;
import com.kb.cosmetic_wms.product.product.domain.model.ProductInfo;
import com.kb.cosmetic_wms.product.product.domain.valueobject.Price;
import com.kb.cosmetic_wms.product.product.domain.valueobject.Volume;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record RegisterProductRequest(

        @NotBlank(message = "브랜드명은 필수 입력 항목입니다.")
        @Size(max = Product.BRAND_NAME_MAX_LENGTH, message = "브랜드명은 100자를 초과할 수 없습니다.")
        String brandName,

        @NotBlank(message = "상품명은 필수 입력 항목입니다.")
        @Size(max = Product.PRODUCT_NAME_MAX_LENGTH, message = "상품명은 255자를 초과할 수 없습니다.")
        String productName,

        @Min(value = Price.MIN_VALUE, message = "상품 가격은 0원 이상이어야 합니다.")
        int productPrice,

        @NotNull(message = "보관 온도 타입은 필수 입력 항목입니다.")
        TemperatureType temperatureType,

        @NotNull(message = "카테고리 ID는 필수 입력 항목입니다.")
        Long categoryId,

        @NotNull(message = "상품 타입 ID는 필수 입력 항목입니다.")
        Long productTypeId,

        @Valid
        @NotNull(message = "상품 상세 정보는 필수 입력 항목입니다.")
        ProductInfoRequest productInfo

) {

    public record ProductInfoRequest(

            @Size(max = ProductInfo.SKIN_TYPE_MAX_LENGTH, message = "피부 타입 정보는 50자를 초과할 수 없습니다.")
            String skinType,

            @Size(max = ProductInfo.FUNCTION_TYPE_MAX_LENGTH, message = "기능성 타입 정보는 100자를 초과할 수 없습니다.")
            String functionType,

            @Valid
            @NotNull(message = "용량 정보는 필수 입력 항목입니다.")
            VolumeRequest volume,

            String ingredients,

            String cautions,

            @Size(max = ProductInfo.STORAGE_CONDITION_MAX_LENGTH, message = "보관 조건 정보는 255자를 초과할 수 없습니다.")
            String storageCondition
    ) {
    }

    public record VolumeRequest(

            @Positive(message = "화장품 용량은 0보다 커야 합니다.")
            @Max(value = Volume.MAX_VALUE, message = "올바르지 않은 대용량 수치입니다. (최대 10000까지 허용)")
            int value,

            @NotBlank(message = "용량 단위는 필수 입력 항목입니다.")
            @Pattern(regexp = Volume.ALLOWED_UNITS_REGEX, message = "올바르지 않은 용량 단위입니다. (ml, g, ea, oz, fl.oz 허용)")
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