package com.kb.cosmetic_wms.domain.store.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreCreateRequestDto(
        @NotBlank(message = "점포명은 필수 입력 항목입니다.")
        String storeName,

        @NotBlank(message = "점포 주소는 필수 입력 항목입니다.")
        String address
) {
}
