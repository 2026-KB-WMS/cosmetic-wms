package com.kb.cosmetic_wms.inventory;

import com.kb.cosmetic_wms.inventory.domain.exception.InvalidShelfLifePolicyException;
import com.kb.cosmetic_wms.inventory.domain.model.ShelfLifePolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShelfLifePolicyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 4);

    private final ShelfLifePolicy policy = new ShelfLifePolicy(7);

    @Test
    void 오늘로부터_최소_잔여일이_지난_날짜를_출고_가능_기준일로_계산한다() {
        assertThat(policy.allocatableAfter(TODAY)).isEqualTo(LocalDate.of(2026, 11, 11));
    }

    @ParameterizedTest(name = "만료일 {0} → 출고 가능 {1}")
    @CsvSource({
            "2026-11-03, false",   // 이미 만료
            "2026-11-04, false",   // 오늘 만료 (잔여 0일)
            "2026-11-10, false",   // 잔여 6일
            "2026-11-11, false",   // 잔여 7일: 7일 이하는 제외
            "2026-11-12, true",    // 잔여 8일: 출고 가능
            "2027-11-04, true",
            "9999-12-31, true"
    })
    void 잔여_유통기한이_최소_잔여일_이하이면_출고할_수_없다(LocalDate expiryDate, boolean expected) {
        assertThat(policy.isAllocatable(expiryDate, TODAY)).isEqualTo(expected);
    }

    @Test
    void 유통기한_정보가_없는_재고는_출고할_수_있다() {
        assertThat(policy.isAllocatable(null, TODAY)).isTrue();
    }

    @Test
    void 최소_잔여일이_0이면_만료되지_않은_재고만_출고할_수_있다() {
        ShelfLifePolicy noBuffer = new ShelfLifePolicy(0);

        assertThat(noBuffer.isAllocatable(TODAY, TODAY)).isFalse();
        assertThat(noBuffer.isAllocatable(TODAY.plusDays(1), TODAY)).isTrue();
    }

    @Test
    void 최소_잔여일이_음수이면_생성할_수_없다() {
        assertThatThrownBy(() -> new ShelfLifePolicy(-1)).isInstanceOf(InvalidShelfLifePolicyException.class);
    }
}
