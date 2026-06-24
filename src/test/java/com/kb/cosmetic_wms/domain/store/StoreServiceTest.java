package com.kb.cosmetic_wms.domain.store;

import com.kb.cosmetic_wms.store.application.port.in.RegisterStoreCommand;
import com.kb.cosmetic_wms.store.application.port.in.StoreResult;
import com.kb.cosmetic_wms.store.application.port.out.StorePort;
import com.kb.cosmetic_wms.store.application.service.StoreService;
import com.kb.cosmetic_wms.store.domain.exception.DuplicateStoreException;
import com.kb.cosmetic_wms.store.domain.exception.StoreErrorCode;
import com.kb.cosmetic_wms.store.domain.exception.StoreNotFoundException;
import com.kb.cosmetic_wms.store.domain.model.Store;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private StorePort storePort;

    @InjectMocks
    private StoreService storeService;

    @Test
    void 존재하는_ID값으로_조회하면_올바른_가맹점_정보를_반환한다() {
        // given
        Long storeId = 1L;
        Store store = Store.reconstitute(storeId, "서울 성수점", "서울시");

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
    void 올바른_가맹점_정보를_입력하면_등록에_성공한다() {
        // given
        RegisterStoreCommand command = new RegisterStoreCommand("부산 서면점", "부산진구");
        Store savedStore = Store.reconstitute(1L, "부산 서면점", "부산진구");

        given(storePort.save(any(Store.class))).willReturn(savedStore);

        // when
        StoreResult result = storeService.register(command);

        // then
        assertThat(result.storeId()).isEqualTo(1L);
        assertThat(result.storeName()).isEqualTo("부산 서면점");
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
}