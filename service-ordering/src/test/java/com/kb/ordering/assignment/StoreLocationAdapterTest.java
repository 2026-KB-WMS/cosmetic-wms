package com.kb.ordering.assignment;

import com.kb.ordering.assignment.adapter.out.external.StoreLocationAdapter;
import com.kb.ordering.global.geocoding.GeoCoordinate;
import com.kb.ordering.store.application.port.in.FindStoreUseCase;
import com.kb.ordering.store.application.port.in.dto.StoreResult;
import com.kb.ordering.store.domain.exception.StoreNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StoreLocationAdapterTest {

    @Mock
    private FindStoreUseCase findStoreUseCase;

    @InjectMocks
    private StoreLocationAdapter storeLocationAdapter;

    @Test
    void 가맹점_ID로_배송지_좌표를_조회한다() {
        // given
        given(findStoreUseCase.findById(1L)).willReturn(new StoreResult(
                1L, "서울 성수점", "서울시 성동구",
                new BigDecimal("37.5445000"), new BigDecimal("127.0560000")));

        // when
        GeoCoordinate coordinate = storeLocationAdapter.loadStoreLocation(1L);

        // then
        assertThat(coordinate.latitude()).isEqualByComparingTo("37.5445");
        assertThat(coordinate.longitude()).isEqualByComparingTo("127.056");
    }

    @Test
    void 존재하지_않는_가맹점이면_store_컨텍스트의_예외가_그대로_전파된다() {
        given(findStoreUseCase.findById(999L)).willThrow(new StoreNotFoundException());

        assertThatThrownBy(() -> storeLocationAdapter.loadStoreLocation(999L))
                .isInstanceOf(StoreNotFoundException.class);
    }
}
