package com.kb.cosmetic_wms.domain.inventory;

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
                .hasMessage("재고 수량은 음수일 수 없습니다.");
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
                .hasMessage("출고 가능 수량은 총 재고 수량을 초과할 수 없습니다.");
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
    void 정상적인_출고_할당_시에_가용_재고가_정상적으로_차감되어야_한다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .quantity(100)
                .availableQuantity(100)
                .build();

        // when
        inventory.allocate(40);

        // then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(60);
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

    @Test
    void 가용재고를_정확히_모두_할당하면_0이되고_추가할당은_실패한다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .availableQuantity(50)
                .build();

        // when
        inventory.allocate(50);

        // then
        assertThat(inventory.getAvailableQuantity()).isZero();

        // when & then
        assertThatThrownBy(() ->
                inventory.allocate(10)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가용 재고가 부족하여 할당할 수 없습니다.");
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
                .hasMessage("할당할 수량은 0보다 커야 합니다.");
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
                .hasMessageContaining("할당 또는 출고 완료된 재고는 품질 상태가 정상이어야 합니다.");
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
                .hasMessageContaining("이미 가맹점 주문 처리 중인 재고는 창고 간 이동(MOVING)을 할 수 없습니다.");
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
                .hasMessage("이미 가맹점 주문 처리 중인 재고는 창고 간 이동(MOVING)을 할 수 없습니다.");
    }

    private static Stream<Arguments> provideInvalidStatusCombinations() {
        return Stream.of(
                Arguments.of(AllocStatus.ALLOCATED, QualityStatus.DISCARD_SCHEDULED, LocStatus.STORED,
                        "할당 또는 출고 완료된 재고는 품질 상태가 정상이어야 합니다."),
                Arguments.of(AllocStatus.SHIPPED, QualityStatus.INSPECTING, LocStatus.STORED,
                        "할당 또는 출고 완료된 재고는 품질 상태가 정상이어야 합니다."),

                Arguments.of(AllocStatus.SHIPPED, QualityStatus.NORMAL, LocStatus.MOVING,
                        "이미 가맹점 주문 처리 중인 재고는 창고 간 이동(MOVING)을 할 수 없습니다."),
                Arguments.of(AllocStatus.ALLOCATED, QualityStatus.NORMAL, LocStatus.MOVING,
                        "이미 가맹점 주문 처리 중인 재고는 창고 간 이동(MOVING)을 할 수 없습니다."),

                Arguments.of(AllocStatus.UNALLOCATED, QualityStatus.INSPECTING, LocStatus.MOVING,
                        "검수 대기/중인 결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다."),
                Arguments.of(AllocStatus.UNALLOCATED, QualityStatus.HOLD, LocStatus.MOVING,
                        "출고 금지인 결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다.")
        );
    }
}
