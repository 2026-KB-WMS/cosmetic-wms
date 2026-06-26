package com.kb.cosmetic_wms.lot;

import com.kb.cosmetic_wms.lot.fixture.LotTestBuilder;
import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import com.kb.cosmetic_wms.lot.domain.exception.InvalidLotNumberFormatException;
import com.kb.cosmetic_wms.lot.domain.exception.InvalidLotStatusTransitionException;
import com.kb.cosmetic_wms.lot.domain.exception.InvalidManufactureDateException;
import com.kb.cosmetic_wms.lot.domain.exception.LotProductIdRequiredException;
import com.kb.cosmetic_wms.lot.domain.model.Lot;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static com.kb.cosmetic_wms.lot.domain.enums.LotStatus.*;

public class LotEntityTest {

    @Test
    void 올바른_로트_번호가_주어졌을_때_Lot_객체가_정상_생성되며_초기_상태는_AVAILABLE이어야_한다() {
        Lot lot = new LotTestBuilder().build();

        Assertions.assertThat(lot.getLotNumber().value()).isEqualTo("SKN-240101-01-0001");
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
                .isInstanceOf(InvalidManufactureDateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SKN240101010001", "skn-240101-01-0001", "SKN-240101-1-0001", "SKN-240101-01-000!"})
    void 로트_번호가_정해진_패턴과_일치하지_않을_때_예외를_던진다(String invalidLotNumber) {
        Assertions.assertThatThrownBy(() ->
                        new LotTestBuilder()
                                .lotNumber(invalidLotNumber)
                                .build()
                )
                .isInstanceOf(InvalidLotNumberFormatException.class);
    }

    @Test
    void 로트_생성_시_상품_식별자_ID가_누락되면_예외를_던진다() {
        Assertions.assertThatThrownBy(() ->
                        new LotTestBuilder()
                                .productId(null)
                                .build()
                )
                .isInstanceOf(LotProductIdRequiredException.class);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class 상태_전환 {

        private Lot lotWithStatus(LotStatus status) {
            return Lot.reconstitute(1L, "SKN-240101-01-0001",
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2027, 1, 1, 0, 0),
                    status, 1L);
        }

        @ParameterizedTest
        @MethodSource("유효한_전환_목록")
        void 허용된_상태로_전환하면_상태가_변경된다(LotStatus from, LotStatus to) {
            Lot lot = lotWithStatus(from);

            lot.changeStatus(to);

            Assertions.assertThat(lot.getStatus()).isEqualTo(to);
        }

        Stream<Arguments> 유효한_전환_목록() {
            return Stream.of(
                    Arguments.of(AVAILABLE, HOLD),
                    Arguments.of(AVAILABLE, EXPIRED),
                    Arguments.of(AVAILABLE, RECALLED),
                    Arguments.of(AVAILABLE, DAMAGED),
                    Arguments.of(AVAILABLE, DISPOSED),
                    Arguments.of(HOLD, AVAILABLE),
                    Arguments.of(HOLD, EXPIRED),
                    Arguments.of(HOLD, RECALLED),
                    Arguments.of(HOLD, DAMAGED),
                    Arguments.of(HOLD, DISPOSED),
                    Arguments.of(EXPIRED, DISPOSED),
                    Arguments.of(RECALLED, DISPOSED),
                    Arguments.of(DAMAGED, DISPOSED)
            );
        }

        @ParameterizedTest
        @MethodSource("유효하지_않은_전환_목록")
        void 허용되지_않은_상태로_전환하면_예외를_던진다(LotStatus from, LotStatus to) {
            Lot lot = lotWithStatus(from);

            Assertions.assertThatThrownBy(() -> lot.changeStatus(to))
                    .isInstanceOf(InvalidLotStatusTransitionException.class);
        }

        Stream<Arguments> 유효하지_않은_전환_목록() {
            return Stream.of(
                    // 자기 자신으로의 전환
                    Arguments.of(AVAILABLE, AVAILABLE),
                    Arguments.of(HOLD, HOLD),
                    // 만료·리콜은 폐기 외 불가
                    Arguments.of(EXPIRED, AVAILABLE),
                    Arguments.of(EXPIRED, HOLD),
                    Arguments.of(EXPIRED, EXPIRED),
                    Arguments.of(EXPIRED, RECALLED),
                    Arguments.of(EXPIRED, DAMAGED),
                    Arguments.of(RECALLED, AVAILABLE),
                    Arguments.of(RECALLED, HOLD),
                    Arguments.of(RECALLED, EXPIRED),
                    Arguments.of(RECALLED, RECALLED),
                    Arguments.of(RECALLED, DAMAGED),
                    // 파손은 폐기 외 불가
                    Arguments.of(DAMAGED, AVAILABLE),
                    Arguments.of(DAMAGED, HOLD),
                    Arguments.of(DAMAGED, EXPIRED),
                    Arguments.of(DAMAGED, RECALLED),
                    Arguments.of(DAMAGED, DAMAGED),
                    // 폐기는 terminal
                    Arguments.of(DISPOSED, AVAILABLE),
                    Arguments.of(DISPOSED, HOLD),
                    Arguments.of(DISPOSED, EXPIRED),
                    Arguments.of(DISPOSED, RECALLED),
                    Arguments.of(DISPOSED, DAMAGED),
                    Arguments.of(DISPOSED, DISPOSED)
            );
        }
    }
}
