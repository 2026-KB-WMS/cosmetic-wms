package com.kb.cosmetic_wms.storage.adapter.in.web;

import com.kb.cosmetic_wms.storage.application.port.in.RegisterWarehouseCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record WarehouseCreateRequest(
        @NotBlank(message = "창고명은 필수입니다.")
        String warehouseName,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotBlank(message = "보관 온도는 필수 입력 항목입니다.")
        @Pattern(
                regexp = "^\\d+~\\d+도$",
                message = "보관 온도 포맷이 올바르지 않습니다. (예: 10~15도)"
        )
        String targetTemp,

        int capacity
) {
    public RegisterWarehouseCommand toCommand() {
        return new RegisterWarehouseCommand(warehouseName, address, targetTemp, capacity);
    }
}