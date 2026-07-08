package com.kb.cosmetic_wms.outbound.adapter.out.external;

import com.kb.cosmetic_wms.order.application.port.in.OrderLifecycleUseCase;
import com.kb.cosmetic_wms.outbound.application.port.out.OrderPreparationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPreparationAdapter implements OrderPreparationPort {

    private final OrderLifecycleUseCase orderLifecycleUseCase;

    @Override
    public void startPreparation(Long orderId) {
        orderLifecycleUseCase.startPreparation(orderId);
    }
}
