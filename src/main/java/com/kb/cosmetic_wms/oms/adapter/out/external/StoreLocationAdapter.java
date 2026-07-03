package com.kb.cosmetic_wms.oms.adapter.out.external;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.oms.application.port.out.LoadStoreLocationPort;
import com.kb.cosmetic_wms.store.application.port.in.FindStoreUseCase;
import com.kb.cosmetic_wms.store.application.port.in.StoreResult;
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
