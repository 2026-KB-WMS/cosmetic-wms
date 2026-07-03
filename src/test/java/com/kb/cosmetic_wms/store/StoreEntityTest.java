package com.kb.cosmetic_wms.store;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.store.domain.exception.StoreValidationException;
import com.kb.cosmetic_wms.store.domain.model.Store;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StoreEntityTest {

    private static final GeoCoordinate COORDINATE = GeoCoordinate.of(53.4308, -2.9608);

    @Test
    void 올바른_데이터를_통한_가맹점_객체를_생성할_수_있다() {
        // given
        String storeName = "리버풀점";
        String address = "Anfield Road, Liverpool, L4 0TH, United Kingdom";

        // when
        Store store = Store.create(storeName, address, COORDINATE);

        // then
        assertThat(store.getStoreName()).isEqualTo("리버풀점");
        assertThat(store.getAddress()).isEqualTo("Anfield Road, Liverpool, L4 0TH, United Kingdom");
        assertThat(store.getCoordinate()).isEqualTo(COORDINATE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void 점포명이_공백이거나_null이면_예외를_던진다(String invalidStoreName) {
        // given
        String address = "Anfield Road, Liverpool, L4 0TH, United Kingdom";

        // when & then
        assertThatThrownBy(() -> Store.create(invalidStoreName, address, COORDINATE))
                .isInstanceOf(StoreValidationException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void 주소가_공백이거나_null이면_예외를_던진다(String invalidAddress) {
        // given
        String storeName = "리버풀점";

        // when & then
        assertThatThrownBy(() -> Store.create(storeName, invalidAddress, COORDINATE))
                .isInstanceOf(StoreValidationException.class);
    }

    @Test
    void 좌표가_null이면_예외를_던진다() {
        assertThatThrownBy(() -> Store.create("리버풀점", "Anfield Road", null))
                .isInstanceOf(StoreValidationException.class);
    }
}
