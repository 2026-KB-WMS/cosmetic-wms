package com.kb.cosmetic_wms.domain.partner;

import com.kb.cosmetic_wms.domain.partner.dto.PartnerCreateRequestDto;
import com.kb.cosmetic_wms.domain.partner.dto.PartnerResponseDto;
import com.kb.cosmetic_wms.domain.partner.entity.Partner;
import com.kb.cosmetic_wms.domain.partner.enums.PartnerType;
import com.kb.cosmetic_wms.domain.partner.exception.DuplicatePartnerException;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerErrorCode;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerNotFoundException;
import com.kb.cosmetic_wms.domain.partner.repository.PartnerRepository;
import com.kb.cosmetic_wms.domain.partner.service.PartnerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PartnerServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private PartnerService partnerService;

    @Test
    void 존재하는_ID값으로_조회하면_올바른_협력사_정보를_반환한다() {
        // given
        Long partnerId = 1L;
        Partner partner = Partner.create("아모레퍼시픽", PartnerType.VENDOR, "120-00-12345");
        ReflectionTestUtils.setField(partner, "id", partnerId);

        given(partnerRepository.findById(partnerId)).willReturn(Optional.of(partner));

        // when
        PartnerResponseDto responseDto = partnerService.findById(partnerId);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.partnerName()).isEqualTo("아모레퍼시픽");
    }

    @Test
    void 존재하지_않는_ID로_조회하면_PartnerNotFoundException을_던진다() {
        Long invalidPartnerId = 999L;
        given(partnerRepository.findById(invalidPartnerId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> partnerService.findById(invalidPartnerId))
                .isInstanceOf(PartnerNotFoundException.class)
                .hasMessage(PartnerErrorCode.PARTNER_NOT_FOUND.getMessage());
    }

    @Test
    void 올바른_협력사_정보를_입력하면_등록에_성공한다() {
        PartnerCreateRequestDto requestDto = new PartnerCreateRequestDto("LG생활건강", PartnerType.VENDOR, "110-11-56789");
        Partner mockPartner = Partner.create(requestDto.partnerName(), requestDto.partnerType(), requestDto.businessNumber());
        ReflectionTestUtils.setField(mockPartner, "id", 1L);

        given(partnerRepository.existsByBusinessNumber(requestDto.businessNumber())).willReturn(false);
        given(partnerRepository.save(any(Partner.class))).willReturn(mockPartner);

        // when
        PartnerResponseDto responseDto = partnerService.register(requestDto);

        // then
        assertThat(responseDto.id()).isEqualTo(1L);
        assertThat(responseDto.partnerName()).isEqualTo("LG생활건강");
        verify(partnerRepository).save(any(Partner.class));
    }

    @Test
    void 이미_존재하는_사업자번호로_등록을_시도하면_예외를_던진다() {
        PartnerCreateRequestDto requestDto = new PartnerCreateRequestDto("LG생활건강", PartnerType.VENDOR, "110-11-56789");
        given(partnerRepository.existsByBusinessNumber(requestDto.businessNumber())).willReturn(true);

        assertThatThrownBy(() -> partnerService.register(requestDto))
                .isInstanceOf(DuplicatePartnerException.class)
                .hasMessage(PartnerErrorCode.DUPLICATE_PARTNER.getMessage());

        verify(partnerRepository, never()).save(any(Partner.class));
    }
}
