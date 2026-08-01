package com.kb.ordering.store.application.port.in;

import com.kb.ordering.store.application.port.in.dto.RegisterStoreCommand;
import com.kb.ordering.store.application.port.in.dto.StoreResult;

public interface RegisterStoreUseCase {
    StoreResult register(RegisterStoreCommand command);
}