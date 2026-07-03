package com.kb.cosmetic_wms.store.application.service;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.global.geocoding.GeocodingPort;
import com.kb.cosmetic_wms.store.application.port.in.FindStoreUseCase;
import com.kb.cosmetic_wms.store.application.port.in.RegisterStoreCommand;
import com.kb.cosmetic_wms.store.application.port.in.RegisterStoreUseCase;
import com.kb.cosmetic_wms.store.application.port.in.StoreResult;
import com.kb.cosmetic_wms.store.application.port.out.StorePort;
import com.kb.cosmetic_wms.store.domain.exception.DuplicateStoreException;
import com.kb.cosmetic_wms.store.domain.exception.StoreNotFoundException;
import com.kb.cosmetic_wms.store.domain.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreService implements RegisterStoreUseCase, FindStoreUseCase {

    private final StorePort storePort;
    private final GeocodingPort geocodingPort;

    @Override
    @Transactional
    public StoreResult register(RegisterStoreCommand command) {
        if (storePort.existsByStoreNameAndAddress(command.storeName(), command.address())) {
            throw new DuplicateStoreException();
        }
        GeoCoordinate coordinate = geocodingPort.geocode(command.address());
        Store store = Store.create(command.storeName(), command.address(), coordinate);
        return StoreResult.from(storePort.save(store));
    }

    @Override
    public StoreResult findById(Long storeId) {
        Store store = storePort.findById(storeId)
                .orElseThrow(StoreNotFoundException::new);
        return StoreResult.from(store);
    }
}