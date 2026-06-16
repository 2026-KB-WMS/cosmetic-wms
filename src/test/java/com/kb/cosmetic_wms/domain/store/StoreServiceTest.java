package com.kb.cosmetic_wms.domain.store;

import com.kb.cosmetic_wms.domain.store.dto.StoreCreateRequestDto;
import com.kb.cosmetic_wms.domain.store.dto.StoreResponseDto;
import com.kb.cosmetic_wms.domain.store.entity.Store;
import com.kb.cosmetic_wms.domain.store.exception.DuplicateStoreException;
import com.kb.cosmetic_wms.domain.store.exception.StoreErrorCode;
import com.kb.cosmetic_wms.domain.store.exception.StoreNotFoundException;
import com.kb.cosmetic_wms.domain.store.repository.StoreRepository;
import com.kb.cosmetic_wms.domain.store.service.StoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    void 존재하는_ID값으로_조회하면_올바른_가맹점_정보를_반환한다() {
        // given
        Long storeId = 1L;
        Store store = Store.create("서울 성수점", "서울시");
        ReflectionTestUtils.setField(store, "id", storeId);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when
        StoreResponseDto responseDto = storeService.findById(storeId);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.storeName()).isEqualTo("서울 성수점");
    }

    @Test
    void 존재하지_않는_ID로_조회하면_예외를_던진다() {
        Long invalidStoreId = 999L;
        given(storeRepository.findById(invalidStoreId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.findById(invalidStoreId))
                .isInstanceOf(StoreNotFoundException.class)
                .hasMessage(StoreErrorCode.STORE_NOT_FOUND.getMessage());
    }

    @Test
    void 올바른_가맹점_정보를_입력하면_등록에_성공한다() {
        StoreCreateRequestDto requestDto = new StoreCreateRequestDto("부산 서면점", "부산진구");
        Store mockStore = Store.create(requestDto.storeName(), requestDto.address());
        ReflectionTestUtils.setField(mockStore, "id", 1L);

        given(storeRepository.save(any(Store.class))).willReturn(mockStore);

        // when
        StoreResponseDto responseDto = storeService.register(requestDto);

        // then
        assertThat(responseDto.id()).isEqualTo(1L);
        assertThat(responseDto.storeName()).isEqualTo("부산 서면점");
        verify(storeRepository).save(any(Store.class));
    }

    @Test
    void 이미_존재하는_점포명과_주소로_등록을_시도하면_예외를_던진다() {
        // given
        StoreCreateRequestDto requestDto = new StoreCreateRequestDto("서울 성수점", "서울시");

        given(storeRepository.existsByStoreNameAndAddress(requestDto.storeName(), requestDto.address()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> storeService.register(requestDto))
                .isInstanceOf(DuplicateStoreException.class)
                .hasMessage(StoreErrorCode.DUPLICATE_STORE.getMessage());

        verify(storeRepository, never()).save(any(Store.class));
    }
}
