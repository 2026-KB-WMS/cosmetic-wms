package com.kb.cosmetic_wms.partner;

import com.kb.cosmetic_wms.partner.application.port.in.PartnerResult;
import com.kb.cosmetic_wms.partner.application.port.in.RegisterPartnerCommand;
import com.kb.cosmetic_wms.partner.application.port.out.PartnerPort;
import com.kb.cosmetic_wms.partner.application.service.PartnerService;
import com.kb.cosmetic_wms.partner.domain.exception.DuplicatePartnerException;
import com.kb.cosmetic_wms.partner.domain.exception.PartnerErrorCode;
import com.kb.cosmetic_wms.partner.domain.exception.PartnerNotFoundException;
import com.kb.cosmetic_wms.partner.domain.model.Partner;
import com.kb.cosmetic_wms.partner.domain.model.PartnerType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PartnerServiceTest {

    @Mock
    private PartnerPort partnerPort;

    @InjectMocks
    private PartnerService partnerService;

    @Test
    void 존재하는_ID값으로_조회하면_올바른_협력사_정보를_반환한다() {
        // given
        Long partnerId = 1L;
        Partner partner = Partner.reconstitute(partnerId, "아모레퍼시픽", PartnerType.VENDOR, "120-00-12345");
        given(partnerPort.findById(partnerId)).willReturn(Optional.of(partner));

        // when
        PartnerResult result = partnerService.findById(partnerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("아모레퍼시픽");
        assertThat(result.partnerId()).isEqualTo(1L);
    }

    @Test
    void 존재하지_않는_ID로_조회하면_PartnerNotFoundException을_던진다() {
        Long invalidPartnerId = 999L;
        given(partnerPort.findById(invalidPartnerId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> partnerService.findById(invalidPartnerId))
                .isInstanceOf(PartnerNotFoundException.class)
                .hasMessage(PartnerErrorCode.PARTNER_NOT_FOUND.getMessage());
    }

    @Test
    void 올바른_협력사_정보를_입력하면_등록에_성공한다() {
        // given
        RegisterPartnerCommand command = new RegisterPartnerCommand("LG생활건강", PartnerType.VENDOR, "110-11-56789");
        Partner saved = Partner.reconstitute(1L, "LG생활건강", PartnerType.VENDOR, "110-11-56789");

        given(partnerPort.existsByBusinessNumber(command.businessNumber())).willReturn(false);
        given(partnerPort.save(any(Partner.class))).willReturn(saved);

        // when
        PartnerResult result = partnerService.register(command);

        // then
        assertThat(result.partnerId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("LG생활건강");
        verify(partnerPort).save(any(Partner.class));
    }

    @Test
    void 이미_존재하는_사업자번호로_등록을_시도하면_예외를_던진다() {
        // given
        RegisterPartnerCommand command = new RegisterPartnerCommand("LG생활건강", PartnerType.VENDOR, "110-11-56789");
        given(partnerPort.existsByBusinessNumber(command.businessNumber())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> partnerService.register(command))
                .isInstanceOf(DuplicatePartnerException.class)
                .hasMessage(PartnerErrorCode.DUPLICATE_PARTNER.getMessage());

        verify(partnerPort, never()).save(any(Partner.class));
    }
}
