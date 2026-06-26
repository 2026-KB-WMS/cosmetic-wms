package com.kb.cosmetic_wms.product.product.adapter.in.web;

import com.kb.cosmetic_wms.product.product.application.port.in.UpdateProductCommand;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.product.product.domain.model.Product;
import com.kb.cosmetic_wms.product.product.domain.model.ProductInfo;
import com.kb.cosmetic_wms.product.product.domain.valueobject.Price;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(

        @NotBlank(message = "상품명은 필수 입력 항목입니다.")
        @Size(max = Product.PRODUCT_NAME_MAX_LENGTH, message = "상품명은 255자를 초과할 수 없습니다.")
        String productName,

        @Min(value = Price.MIN_VALUE, message = "상품 가격은 0원 이상이어야 합니다.")
        int productPrice,

        @NotNull(message = "보관 온도 타입은 필수 입력 항목입니다.")
        TemperatureType temperatureType,

        @Valid
        @NotNull(message = "상품 상세 정보는 필수 입력 항목입니다.")
        ProductInfoUpdateRequest productInfo

) {

    public record ProductInfoUpdateRequest(

            @Size(max = ProductInfo.SKIN_TYPE_MAX_LENGTH, message = "피부 타입 정보는 50자를 초과할 수 없습니다.")
            String skinType,

            @Size(max = ProductInfo.FUNCTION_TYPE_MAX_LENGTH, message = "기능성 타입 정보는 100자를 초과할 수 없습니다.")
            String functionType,

            String ingredients,

            String cautions,

            @Size(max = ProductInfo.STORAGE_CONDITION_MAX_LENGTH, message = "보관 조건 정보는 255자를 초과할 수 없습니다.")
            String storageCondition
    ) {
    }

    public UpdateProductCommand toCommand() {
        return new UpdateProductCommand(
                productName, productPrice, temperatureType,
                productInfo.skinType(), productInfo.functionType(),
                productInfo.ingredients(), productInfo.cautions(), productInfo.storageCondition()
        );
    }
}