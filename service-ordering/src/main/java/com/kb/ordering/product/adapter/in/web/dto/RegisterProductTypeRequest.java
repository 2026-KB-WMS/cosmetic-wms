package com.kb.ordering.product.adapter.in.web.dto;

import com.kb.ordering.product.application.port.in.dto.RegisterProductTypeCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterProductTypeRequest(
        @NotBlank(message = "타입 코드는 필수 입력 항목입니다.")
        @Size(min = 3, max = 3, message = "타입 코드는 3자리여야 합니다.")
        @Pattern(regexp = "^[A-Z]+$", message = "타입 코드는 영문 대문자만 가능합니다.")
        String typeCode,

        @NotBlank(message = "타입 이름은 필수 입력 항목입니다.")
        String typeName
) {
    public RegisterProductTypeCommand toCommand() {
        return new RegisterProductTypeCommand(typeCode, typeName);
    }
}
