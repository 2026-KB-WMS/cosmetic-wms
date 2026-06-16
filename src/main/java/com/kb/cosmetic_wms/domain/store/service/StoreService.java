package com.kb.cosmetic_wms.domain.store.service;

import com.kb.cosmetic_wms.domain.store.dto.StoreCreateRequestDto;
import com.kb.cosmetic_wms.domain.store.dto.StoreResponseDto;
import com.kb.cosmetic_wms.domain.store.entity.Store;
import com.kb.cosmetic_wms.domain.store.exception.DuplicateStoreException;
import com.kb.cosmetic_wms.domain.store.exception.StoreNotFoundException;
import com.kb.cosmetic_wms.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreResponseDto findById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(StoreNotFoundException::new);

        return StoreResponseDto.from(store);
    }

    /**
     * 신규 가맹점 등록
     */
    @Transactional
    public StoreResponseDto register(StoreCreateRequestDto requestDto) {
        if (storeRepository.existsByStoreNameAndAddress(requestDto.storeName(), requestDto.address())) {
            throw new DuplicateStoreException();
        }
        Store store = Store.create(requestDto.storeName(), requestDto.address());
        Store savedStore = storeRepository.save(store);

        return StoreResponseDto.from(savedStore);
    }
}
