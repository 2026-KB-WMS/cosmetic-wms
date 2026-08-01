package com.kb.ordering.store.application.service;

import com.kb.ordering.global.geocoding.GeoCoordinate;
import com.kb.ordering.global.geocoding.GeocodingPort;
import com.kb.ordering.store.application.port.in.FindStoreUseCase;
import com.kb.ordering.store.application.port.in.RegisterStoreUseCase;
import com.kb.ordering.store.application.port.in.dto.RegisterStoreCommand;
import com.kb.ordering.store.application.port.in.dto.StoreResult;
import com.kb.ordering.store.application.port.out.StorePort;
import com.kb.ordering.store.domain.exception.DuplicateStoreException;
import com.kb.ordering.store.domain.exception.StoreNotFoundException;
import com.kb.ordering.store.domain.model.Store;
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