package com.kb.cosmetic_wms.domain.inventory;

import com.kb.cosmetic_wms.domain.inventory.constants.InventoryConstants;
import com.kb.cosmetic_wms.domain.inventory.entity.Inventory;
import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;
import com.kb.cosmetic_wms.domain.inventory.fixture.InventoryTestBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InventoryEntityTest {

    @Test
    void 재고_생성_시_수량이_음수이면_예외를_던진다() {
        assertThatThrownBy(() ->
                new InventoryTestBuilder()
                        .quantity(-5)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.INVALID_QUANTITY_MESSAGE);
    }

    @Test
    void 재고_생성_시_출고_가능_수량이_총_재고_수량을_초과하면_예외를_던진다() {
        assertThatThrownBy(() ->
                new InventoryTestBuilder()
                        .quantity(100)
                        .availableQuantity(101)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.OVER_AVAILABLE_QUANTITY_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = QualityStatus.class, names = {"INSPECTING", "HOLD", "DISCARD_SCHEDULED"})
    void 재고_생성_시_품질_상태가_정상이_아니라면_출고_가능_수량은_0이어야_한다(QualityStatus status) {
        assertThatThrownBy(() ->
                new InventoryTestBuilder()
                        .qualityStatus(status)
                        .availableQuantity(50)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("출고 가능 수량은 0이어야 합니다.");
    }

    @Test
    void 정상적인_재고에서_일부_수량을_할당하면_원래_재고는_수량이_깎이고_할당된_새로운_재고_객체가_반환된다() {
        // given
        Inventory originalInventory = new InventoryTestBuilder()
                .quantity(100)
                .availableQuantity(100)
                .allocStatus(AllocStatus.UNALLOCATED)
                .build();

        // when
        Inventory allocatedInventory = originalInventory.allocate(40);

        // then 1: 원본 재고는 60개로 깎이고 여전히 미할당(UNALLOCATED) 상태여야 함
        assertThat(originalInventory.getQuantity()).isEqualTo(60);
        assertThat(originalInventory.getAvailableQuantity()).isEqualTo(60);
        assertThat(originalInventory.getStatusSet().allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);

        // then 2: 분할되어 나온 재고는 40개이며 할당됨(ALLOCATED) 상태여야 함
        assertThat(allocatedInventory.getQuantity()).isEqualTo(40);
        assertThat(allocatedInventory.getAvailableQuantity()).isEqualTo(0);
        assertThat(allocatedInventory.getStatusSet().allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
    }

    @Test
    void 원래_재고의_가용_수량을_정확히_전부_할당하면_새_객체를_만들지_않고_자신이_ALLOCATED로_변환된다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(50)
                .allocStatus(AllocStatus.UNALLOCATED)
                .build();

        // when
        Inventory resultInventory = inventory.allocate(50);

        assertThat(resultInventory).isSameAs(inventory);
        assertThat(inventory.getQuantity()).isEqualTo(50);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(0);
        assertThat(inventory.getStatusSet().allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
    }

    @Test
    void 가용_재고를_초과하여_할당을_시도하면_예외를_던진다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(50)
                .build();

        // when & then
        assertThatThrownBy(() ->
                inventory.allocate(60)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.LACK_OF_AVAILABLE_QUANTITY_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = QualityStatus.class, names = {"HOLD", "DISCARD_SCHEDULED"})
    void 품질_상태가_불량으로_변할_때_가용_수량이_0으로_변경되어야_한다(QualityStatus qualityStatus) {
        // given
        Inventory inventory = new InventoryTestBuilder().build();

        // when
        inventory.changeQualityStatus(qualityStatus);

        // then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10, -100})
    void 할당_수량에_0_이하의_값이_들어오면_예외를_던진다(int invalidQuantity) {
        // given
        Inventory inventory = new InventoryTestBuilder().build();

        // when & then
        assertThatThrownBy(() ->
                inventory.allocate(invalidQuantity)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.INVALID_ALLOCATE_QUANTITY_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidStatusCombinations")
    void 재고_생성_시_불가능한_상태_조합이_유입되면_예외를_던진다(
            AllocStatus allocStatus, QualityStatus qualityStatus, LocStatus locStatus, String expectedMsg
    ) {
        assertThatThrownBy(() ->
                new InventoryTestBuilder()
                        .allocStatus(allocStatus)
                        .qualityStatus(qualityStatus)
                        .locStatus(locStatus)
                        .availableQuantity(0)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMsg);
    }

    @Test
    void 이미_할당된_재고의_품질을_불량으로_변경하려고_하면_예외를_던진다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .qualityStatus(QualityStatus.NORMAL)
                .locStatus(LocStatus.STORED)
                .build();

        // when & then
        assertThatThrownBy(() ->
                inventory.changeQualityStatus(QualityStatus.HOLD)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(InventoryConstants.INVALID_STATUS_SET_QUALITY_MESSAGE);
    }

    @Test
    void 이미_할당된_재고를_창고_간_이동_상태로_변경하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .qualityStatus(QualityStatus.NORMAL)
                .locStatus(LocStatus.STORED)
                .build();

        Assertions.assertThatThrownBy(() ->
                        inventory.changeLocStatus(LocStatus.MOVING)
                )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(InventoryConstants.INVALID_STATUS_SET_ALLOC_MOVING_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = QualityStatus.class, names = {"INSPECTING", "HOLD", "DISCARD_SCHEDULED"})
    void 검수_중이거나_출고_금지_상태_또는_폐기_예정인_재고를_창고_간_이동_상태로_변경하려고_하면_예외를_던진다(
            QualityStatus qualityStatus
    ) {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.UNALLOCATED)
                .qualityStatus(qualityStatus)
                .locStatus(LocStatus.STORED)
                .availableQuantity(0)
                .build();

        // when & then
        assertThatThrownBy(() ->
                inventory.changeLocStatus(LocStatus.MOVING)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다.");
    }

    @Test
    void 정상적으로_창고_간_이동_중인_재고에_출고_할당을_시도하면_예외를_던진다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.UNALLOCATED)
                .qualityStatus(QualityStatus.NORMAL)
                .locStatus(LocStatus.MOVING)
                .build();

        // when & then
        assertThatThrownBy(() ->
                inventory.allocate(10)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.INVALID_STATUS_SET_ALLOC_MOVING_MESSAGE);
    }

    private static Stream<Arguments> provideInvalidStatusCombinations() {
        return Stream.of(
                Arguments.of(AllocStatus.ALLOCATED, QualityStatus.DISCARD_SCHEDULED, LocStatus.STORED,
                        InventoryConstants.INVALID_STATUS_SET_QUALITY_MESSAGE),
                Arguments.of(AllocStatus.SHIPPED, QualityStatus.INSPECTING, LocStatus.STORED,
                        InventoryConstants.INVALID_STATUS_SET_QUALITY_MESSAGE),

                Arguments.of(AllocStatus.SHIPPED, QualityStatus.NORMAL, LocStatus.MOVING,
                        InventoryConstants.INVALID_STATUS_SET_ALLOC_MOVING_MESSAGE),
                Arguments.of(AllocStatus.ALLOCATED, QualityStatus.NORMAL, LocStatus.MOVING,
                        InventoryConstants.INVALID_STATUS_SET_ALLOC_MOVING_MESSAGE),

                Arguments.of(AllocStatus.UNALLOCATED, QualityStatus.INSPECTING, LocStatus.MOVING,
                        "창고 간 이동(MOVING)이 불가능합니다."),
                Arguments.of(AllocStatus.UNALLOCATED, QualityStatus.HOLD, LocStatus.MOVING,
                        "창고 간 이동(MOVING)이 불가능합니다.")
        );
    }
}
