package com.kb.cosmetic_wms.store.application.port.in;

public interface RegisterStoreUseCase {
    StoreResult register(RegisterStoreCommand command);
}