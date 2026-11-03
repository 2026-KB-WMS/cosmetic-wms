package com.kb.cosmetic_wms.storage;

import com.kb.cosmetic_wms.product.product.application.port.in.FindProductUseCase;
import com.kb.cosmetic_wms.storage.adapter.out.external.ProductTemperatureQueryAdapter;
import com.kb.cosmetic_wms.storage.domain.exception.StorageValidationException;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductTemperatureQueryAdapterTest {

    @InjectMocks
    private ProductTemperatureQueryAdapter adapter;

    @Mock
    private FindProductUseCase findProductUseCase;

    @Test
    void 상품의_온도_구분_코드를_온도_구역으로_변환한다() {
        // given
        given(findProductUseCase.findTemperatureCodesByIds(List.of(1L, 2L)))
                .willReturn(Map.of(1L, "R", 2L, "C"));

        // when
        Map<Long, TemperatureZone> result = adapter.findTemperatureZonesByIds(List.of(1L, 2L));

        // then
        assertThat(result).containsEntry(1L, TemperatureZone.ROOM).containsEntry(2L, TemperatureZone.COOL);
    }

    @Test
    void 알_수_없는_온도_구분_코드가_오면_StorageValidationException_예외를_던진다() {
        // given
        given(findProductUseCase.findTemperatureCodesByIds(List.of(1L))).willReturn(Map.of(1L, "Z"));

        // when & then
        assertThatThrownBy(() -> adapter.findTemperatureZonesByIds(List.of(1L)))
                .isInstanceOf(StorageValidationException.class);
    }
}
