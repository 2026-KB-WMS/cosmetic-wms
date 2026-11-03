package com.kb.cosmetic_wms.inspection;

import com.kb.cosmetic_wms.inspection.adapter.out.external.LotQueryAdapter;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionLotNotFoundException;
import com.kb.cosmetic_wms.lot.application.port.in.FindLotUseCase;
import com.kb.cosmetic_wms.lot.application.port.in.LotResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LotQueryAdapterTest {

    @InjectMocks
    private LotQueryAdapter adapter;

    @Mock
    private FindLotUseCase findLotUseCase;

    @Test
    void 입고_ID와_제조사_로트_번호로_로트_ID를_조회한다() {
        // given
        LotResult lot = new LotResult(7L, 10L, "LOT0001", LocalDateTime.now(), LocalDateTime.now().plusDays(1), null, 1L);
        given(findLotUseCase.findByInboundIdAndManufacturerLotNumber(10L, "LOT0001")).willReturn(Optional.of(lot));

        // when
        Long lotId = adapter.findLotIdByInboundAndManufacturerLot(10L, "LOT0001");

        // then
        assertThat(lotId).isEqualTo(7L);
    }

    @Test
    void 로트가_없으면_InspectionLotNotFoundException_예외를_던진다() {
        // given
        given(findLotUseCase.findByInboundIdAndManufacturerLotNumber(10L, "NONE")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adapter.findLotIdByInboundAndManufacturerLot(10L, "NONE"))
                .isInstanceOf(InspectionLotNotFoundException.class);
    }
}
