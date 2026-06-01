package com.kb.cosmetic_wms.domain.inventory;

import com.kb.cosmetic_wms.domain.inventory.constants.LotConstants;
import com.kb.cosmetic_wms.domain.inventory.entity.Lot;
import com.kb.cosmetic_wms.domain.inventory.enums.LotStatus;
import com.kb.cosmetic_wms.domain.inventory.fixture.LotTestBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

public class LotEntityTest {

    @Test
    void 올바른_로트_번호가_주어졌을_때_Lot_객체가_정상_생성되며_초기_상태는_AVAILABLE이어야_한다() {
        // given & when
        Lot lot = new LotTestBuilder().build();

        // then
        Assertions.assertThat(lot.getLotNumber()).isEqualTo("SKN-240101-01-0001");
        Assertions.assertThat(lot.getStatus()).isEqualTo(LotStatus.AVAILABLE);
    }

    @Test
    void 제조일자가_유통기한보다_미래일_때_예외를_던진다() {
        Assertions.assertThatThrownBy(() ->
                        new LotTestBuilder()
                                .manufacturingDate(LocalDateTime.of(2027, 5, 1, 0, 0))
                                .expirationDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                                .build()
                )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(LotConstants.INVALID_MANUFACTURE_DATE_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SKN240101010001", "skn-240101-01-0001", "SKN-240101-1-0001", "SKN-240101-01-000!"})
    void 로트_번호가_정해진_패턴과_일치하지_않을_때_예외를_던진다(String invalidLotNumber) {
        Assertions.assertThatThrownBy(() ->
                        new LotTestBuilder()
                                .lotNumber(invalidLotNumber)
                                .build()
                )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(LotConstants.INVALID_LOT_NO_FORMAT_MESSAGE);
    }
}
