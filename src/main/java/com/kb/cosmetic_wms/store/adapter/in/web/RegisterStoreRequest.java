package com.kb.cosmetic_wms.store.adapter.in.web;

import com.kb.cosmetic_wms.store.application.port.in.RegisterStoreCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterStoreRequest(
        @NotBlank(message = "점포명은 필수 입력 항목입니다.")
        @Size(max = 100, message = "점포명은 100자 이하로 입력해주세요.")
        String storeName,

        @NotBlank(message = "점포 주소는 필수 입력 항목입니다.")
        @Size(max = 100, message = "점포 주소는 100자 이하로 입력해주세요.")
        String address
) {
    public RegisterStoreCommand toCommand() {
        return new RegisterStoreCommand(storeName, address);
    }
}