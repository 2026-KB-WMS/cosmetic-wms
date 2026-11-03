package com.kb.cosmetic_wms.lot;

import com.kb.cosmetic_wms.lot.application.port.in.LotResult;
import com.kb.cosmetic_wms.lot.application.port.out.LotPort;
import com.kb.cosmetic_wms.lot.application.service.LotService;
import com.kb.cosmetic_wms.lot.fixture.LotTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FindLotByInboundServiceTest {

    @InjectMocks
    private LotService lotService;

    @Mock
    private LotPort lotPort;

    @Test
    void 입고_ID와_제조사_로트_번호로_로트를_조회한다() {
        // given
        given(lotPort.findByInboundIdAndManufacturerLotNumber(10L, "LOT0001"))
                .willReturn(Optional.of(new LotTestBuilder().inboundId(10L).manufacturerLotNumber("LOT0001").buildWithId(7L)));

        // when
        Optional<LotResult> result = lotService.findByInboundIdAndManufacturerLotNumber(10L, "LOT0001");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(7L);
    }

    @Test
    void 일치하는_로트가_없으면_빈_결과를_반환한다() {
        // given
        given(lotPort.findByInboundIdAndManufacturerLotNumber(10L, "NONE")).willReturn(Optional.empty());

        // when & then
        assertThat(lotService.findByInboundIdAndManufacturerLotNumber(10L, "NONE")).isEmpty();
    }
}
