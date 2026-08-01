package com.kb.ordering.assignment.adapter.out.external;

import com.kb.ordering.assignment.application.port.out.LoadStoreLocationPort;
import com.kb.ordering.global.geocoding.GeoCoordinate;
import com.kb.ordering.store.application.port.in.FindStoreUseCase;
import com.kb.ordering.store.application.port.in.dto.StoreResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreLocationAdapter implements LoadStoreLocationPort {

    private final FindStoreUseCase findStoreUseCase;

    @Override
    public GeoCoordinate loadStoreLocation(Long storeId) {
        StoreResult store = findStoreUseCase.findById(storeId);
        return new GeoCoordinate(store.latitude(), store.longitude());
    }
}
