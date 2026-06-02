package com.kb.cosmetic_wms.domain.store;

import com.kb.cosmetic_wms.domain.store.entity.Store;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StoreEntityTest {

    @Test
    void 올바른_데이터를_통한_가맹점_객체를_생성할_수_있다() {
        // given
        String storeName = "리버풀점";
        String address = "Anfield Road, Liverpool, L4 0TH, United Kingdom";

        // when
        Store store = Store.create(storeName, address);

        // then
        assertThat(store.getStoreName()).isEqualTo("리버풀점");
        assertThat(store.getAddress()).isEqualTo("Anfield Road, Liverpool, L4 0TH, United Kingdom");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void 점포명이_공백이거나_null이면_예외를_던진다(String invalidStoreName) {
        // given
        String address = "Anfield Road, Liverpool, L4 0TH, United Kingdom";

        // when & then
        assertThatThrownBy(() ->
                Store.create(invalidStoreName, address)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("점포명은 필수 입력 항목입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void 주소가_공백이거나_null이면_예외를_던진다(String invalidAddress) {
        // given
        String storeName = "리버풀점";

        // when & then
        assertThatThrownBy(() ->
                Store.create(storeName, invalidAddress)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("점포 주소는 필수 입력 항목입니다.");
    }
}
