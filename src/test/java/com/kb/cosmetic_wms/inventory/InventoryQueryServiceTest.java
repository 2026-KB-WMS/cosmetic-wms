package com.kb.cosmetic_wms.inventory;

import com.kb.cosmetic_wms.inventory.application.port.in.InventoryResult;
import com.kb.cosmetic_wms.inventory.application.port.out.InventoryPort;
import com.kb.cosmetic_wms.inventory.application.service.InventoryQueryService;
import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryErrorCode;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryNotFoundException;
import com.kb.cosmetic_wms.inventory.domain.model.Inventory;
import com.kb.cosmetic_wms.inventory.fixture.InventoryTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InventoryQueryServiceTest {

    @InjectMocks
    private InventoryQueryService inventoryQueryService;

    @Mock
    private InventoryPort inventoryPort;

    private Inventory defaultInventory;

    @BeforeEach
    void setUp() {
        defaultInventory = new InventoryTestBuilder().build();
        ReflectionTestUtils.setField(defaultInventory, "id", 1L);
    }

    @Test
    void 존재하는_ID로_조회하면_재고_상세정보를_반환한다() {
        given(inventoryPort.findById(1L)).willReturn(Optional.of(defaultInventory));

        InventoryResult result = inventoryQueryService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.quantity()).isEqualTo(100);
        assertThat(result.availableQuantity()).isEqualTo(100);
        assertThat(result.allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);
        assertThat(result.qualityStatus()).isEqualTo(QualityStatus.NORMAL);
        assertThat(result.locStatus()).isEqualTo(LocStatus.STORED);
    }

    @Test
    void 존재하지_않는_ID로_조회하면_InventoryNotFoundException이_발생한다() {
        given(inventoryPort.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryQueryService.findById(999L))
                .isInstanceOf(InventoryNotFoundException.class)
                .hasMessage(InventoryErrorCode.INVENTORY_NOT_FOUND.getMessage());
    }

    @Test
    void LOT_ID로_조회하면_해당_LOT의_전체_재고_목록을_반환한다() {
        Inventory secondInventory = new InventoryTestBuilder().quantity(50).availableQuantity(50).build();
        ReflectionTestUtils.setField(secondInventory, "id", 2L);

        given(inventoryPort.findByLotId(10L)).willReturn(List.of(defaultInventory, secondInventory));

        List<InventoryResult> result = inventoryQueryService.findByLotId(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).quantity()).isEqualTo(50);
    }

    @Test
    void 해당_LOT에_재고가_없으면_빈_목록을_반환한다() {
        given(inventoryPort.findByLotId(10L)).willReturn(List.of());

        List<InventoryResult> result = inventoryQueryService.findByLotId(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void 상품_ID로_조회하면_해당_상품의_전체_재고_목록을_반환한다() {
        given(inventoryPort.findByProductId(1L)).willReturn(List.of(defaultInventory));

        List<InventoryResult> result = inventoryQueryService.findByProductId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

    @Test
    void 상품_ID_목록이_비어있으면_가용성_조회는_빈_목록을_반환한다() {
        assertThat(inventoryQueryService.findAvailabilityByProducts(List.of())).isEmpty();
        assertThat(inventoryQueryService.findAvailabilityByProducts(null)).isEmpty();
    }
}
