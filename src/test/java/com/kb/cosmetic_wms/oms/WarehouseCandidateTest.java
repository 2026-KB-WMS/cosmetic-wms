package com.kb.cosmetic_wms.oms;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.oms.domain.exception.OmsValidationException;
import com.kb.cosmetic_wms.oms.domain.model.ProductStock;
import com.kb.cosmetic_wms.oms.domain.model.WarehouseCandidate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarehouseCandidateTest {

    private static final GeoCoordinate COORDINATE = GeoCoordinate.of(37.4602, 126.4407);

    @Test
    void 상품별_가용_수량과_최근접_유통기한을_조회할_수_있다() {
        LocalDate expiry = LocalDate.of(2026, 12, 31);
        WarehouseCandidate candidate = WarehouseCandidate.of(1L, COORDINATE,
                Map.of(100L, new ProductStock(30, expiry)));

        assertThat(candidate.availableQuantityOf(100L)).isEqualTo(30);
        assertThat(candidate.earliestExpiryOf(100L)).isEqualTo(expiry);
    }

    @Test
    void 재고_요약에_없는_상품은_가용_수량_0으로_취급한다() {
        WarehouseCandidate candidate = WarehouseCandidate.of(1L, COORDINATE, Map.of());

        assertThat(candidate.availableQuantityOf(999L)).isZero();
        assertThat(candidate.earliestExpiryOf(999L)).isNull();
        assertThat(candidate.hasAnyStock()).isFalse();
    }

    @Test
    void 하나라도_가용_재고가_있으면_hasAnyStock은_true를_반환한다() {
        WarehouseCandidate candidate = WarehouseCandidate.of(1L, COORDINATE,
                Map.of(100L, new ProductStock(1, LocalDate.of(2026, 12, 31))));

        assertThat(candidate.hasAnyStock()).isTrue();
    }

    @Test
    void 창고_ID나_좌표가_없으면_예외를_던진다() {
        assertThatThrownBy(() -> WarehouseCandidate.of(null, COORDINATE, Map.of()))
                .isInstanceOf(OmsValidationException.class);
        assertThatThrownBy(() -> WarehouseCandidate.of(1L, null, Map.of()))
                .isInstanceOf(OmsValidationException.class);
    }

    @Test
    void 가용_재고가_있는데_유통기한이_없는_재고_요약은_생성할_수_없다() {
        assertThatThrownBy(() -> new ProductStock(10, null))
                .isInstanceOf(OmsValidationException.class);
    }
}
