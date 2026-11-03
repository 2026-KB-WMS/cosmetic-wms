package com.kb.cosmetic_wms.product;

import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.product.application.service.ProductService;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductTemperatureLookupServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductPort productPort;

    @Test
    void 상품별_보관_온도를_구분_코드로_반환한다() {
        // given
        given(productPort.findTemperatureTypesByIds(List.of(1L, 2L)))
                .willReturn(Map.of(1L, TemperatureType.ROOM, 2L, TemperatureType.COOL));

        // when
        Map<Long, String> result = productService.findTemperatureCodesByIds(List.of(1L, 2L));

        // then
        assertThat(result).containsEntry(1L, "R").containsEntry(2L, "C");
    }
}
