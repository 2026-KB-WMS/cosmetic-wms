package com.kb.cosmetic_wms.domain.partner.dto;

import com.kb.cosmetic_wms.domain.partner.enums.PartnerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PartnerCreateRequestDto(
        @NotBlank(message = "파트너명은 필수 항목입니다.")
        @Size(max = 100, message = "파트너명은 100자 이하로 입력해주세요.")
        String partnerName,

        @NotNull(message = "파트너 타입은 필수 항목입니다.")
        PartnerType partnerType,

        @NotBlank(message = "사업자 번호는 필수 항목입니다.")
        @Size(max = 20, message = "사업자 번호는 20자 이하로 입력해주세요.")
        String businessNumber
) {
}
