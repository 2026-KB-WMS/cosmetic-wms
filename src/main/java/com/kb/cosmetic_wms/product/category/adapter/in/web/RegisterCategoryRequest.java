package com.kb.cosmetic_wms.product.category.adapter.in.web;

import com.kb.cosmetic_wms.product.category.application.port.in.RegisterCategoryCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterCategoryRequest(
        @NotBlank(message = "카테고리 코드는 필수 입력 항목입니다.")
        @Size(min = 3, max = 3, message = "카테고리 코드는 3자리여야 합니다.")
        @Pattern(regexp = "^[A-Z]+$", message = "카테고리 코드는 영문 대문자만 가능합니다.")
        String categoryCode,

        @NotBlank(message = "카테고리 이름은 필수 입력 항목입니다.")
        String categoryName
) {
    public RegisterCategoryCommand toCommand() {
        return new RegisterCategoryCommand(categoryCode, categoryName);
    }
}