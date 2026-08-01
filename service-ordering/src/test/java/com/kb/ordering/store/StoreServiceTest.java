package com.kb.ordering.store;

import com.kb.ordering.global.geocoding.GeoCoordinate;
import com.kb.ordering.global.geocoding.GeocodingErrorCode;
import com.kb.ordering.global.geocoding.GeocodingFailedException;
import com.kb.ordering.global.geocoding.GeocodingPort;
import com.kb.ordering.store.application.port.in.dto.RegisterStoreCommand;
import com.kb.ordering.store.application.port.in.dto.StoreResult;
import com.kb.ordering.store.application.port.out.StorePort;
import com.kb.ordering.store.application.service.StoreService;
import com.kb.ordering.store.domain.exception.DuplicateStoreException;
import com.kb.ordering.store.domain.exception.StoreErrorCode;
import com.kb.ordering.store.domain.exception.StoreNotFoundException;
import com.kb.ordering.store.domain.model.Store;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

    private static final GeoCoordinate COORDINATE = GeoCoordinate.of(37.5445, 127.0560);

    @Mock
    private StorePort storePort;

    @Mock
    private GeocodingPort geocodingPort;

    @InjectMocks
    private StoreService storeService;

    @Test
    void 존재하는_ID값으로_조회하면_올바른_가맹점_정보를_반환한다() {
        // given
        Long storeId = 1L;
        Store store = Store.reconstitute(storeId, "서울 성수점", "서울시", COORDINATE);

        given(storePort.findById(storeId)).willReturn(Optional.of(store));

        // when
        StoreResult result = storeService.findById(storeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.storeName()).isEqualTo("서울 성수점");
    }

    @Test
    void 존재하지_않는_ID로_조회하면_예외를_던진다() {
        // given
        Long invalidStoreId = 999L;
        given(storePort.findById(invalidStoreId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.findById(invalidStoreId))
                .isInstanceOf(StoreNotFoundException.class)
                .hasMessage(StoreErrorCode.STORE_NOT_FOUND.getMessage());
    }

    @Test
    void 올바른_가맹점_정보를_입력하면_주소가_좌표로_변환되어_등록에_성공한다() {
        // given
        RegisterStoreCommand command = new RegisterStoreCommand("부산 서면점", "부산진구");
        Store savedStore = Store.reconstitute(1L, "부산 서면점", "부산진구", COORDINATE);

        given(geocodingPort.geocode(command.address())).willReturn(COORDINATE);
        given(storePort.save(any(Store.class))).willReturn(savedStore);

        // when
        StoreResult result = storeService.register(command);

        // then
        assertThat(result.storeId()).isEqualTo(1L);
        assertThat(result.storeName()).isEqualTo("부산 서면점");
        assertThat(result.latitude()).isEqualTo(COORDINATE.latitude());
        assertThat(result.longitude()).isEqualTo(COORDINATE.longitude());
        verify(storePort).save(any(Store.class));
    }

    @Test
    void 이미_존재하는_점포명과_주소로_등록을_시도하면_예외를_던진다() {
        // given
        RegisterStoreCommand command = new RegisterStoreCommand("서울 성수점", "서울시");

        given(storePort.existsByStoreNameAndAddress(command.storeName(), command.address()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> storeService.register(command))
                .isInstanceOf(DuplicateStoreException.class)
                .hasMessage(StoreErrorCode.DUPLICATE_STORE.getMessage());

        verify(storePort, never()).save(any(Store.class));
    }

    @Test
    void 주소_좌표_변환에_실패하면_가맹점이_저장되지_않고_예외를_던진다() {
        // given
        RegisterStoreCommand command = new RegisterStoreCommand("부산 서면점", "존재하지 않는 주소");
        given(geocodingPort.geocode(anyString()))
                .willThrow(new GeocodingFailedException(GeocodingErrorCode.GEOCODING_ADDRESS_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> storeService.register(command))
                .isInstanceOf(GeocodingFailedException.class);

        verify(storePort, never()).save(any(Store.class));
    }
}
