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

    @Test
    void 품질_상태가_불량으로_변할_때_원본_재고의_가용_수량이_차감되고_새_재고의_가용_수량은_0이어야_한다() {
        // given
        Inventory inventory = new InventoryTestBuilder().build();
        int targetQuantity = 30;

        // when
        Inventory brokenInventory = inventory.holdForQualityIssue(targetQuantity);

        // then 1: 원본 재고 검증 (100개 중 30개가 빠져나가서 70개의 가용 수량 남아야 함)
        assertThat(inventory.getAvailableQuantity()).isEqualTo(70);
        assertThat(inventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.NORMAL);

        // then 2: 분할된 불량 재고 검증 (수량은 30개지만, 불량이므로 가용 수량은 0이어야 함)
        assertThat(brokenInventory.getQuantity()).isEqualTo(targetQuantity);
        assertThat(brokenInventory.getAvailableQuantity()).isZero();
        assertThat(brokenInventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.HOLD);
    }

    @Test
    void 전체_수량의_품질_상태가_불량으로_변할_때는_객체_분할_없이_자기_자신의_가용_수량이_0이_되어야_한다() {
        // given
        int totalQuantity = 50;
        Inventory inventory = new InventoryTestBuilder()
                .quantity(totalQuantity)
                .availableQuantity(totalQuantity)
                .build();

        // when
        Inventory resultInventory = inventory.holdForQualityIssue(totalQuantity);

        // then
        assertThat(resultInventory).isSameAs(inventory);
        assertThat(inventory.getQuantity()).isEqualTo(totalQuantity);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(0);
        assertThat(inventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.HOLD);
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
    void 이미_할당된_재고의_품질을_폐기_예정으로_변경하려고_하면_예외를_던진다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        // when & then
        assertThatThrownBy(() ->
                inventory.scheduleForDiscard(10)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(InventoryConstants.DISCARD_FOR_UNALLOCATED_ONLY_MESSAGE);
    }

    @Test
    void 이미_할당된_재고를_창고_간_이동_상태로_변경하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        Assertions.assertThatThrownBy(() ->
                        inventory.startMoving(20)
                )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(InventoryConstants.START_MOVING_FOR_UNALLOCATED_ONLY_MESSAGE);
    }

    @Test
    void 일부_수량을_할당_취소하면_원래_재고의_수량은_유지되고_가용_수량이_복구된_새_객체가_반환된다() {
        // given: inventory의 수량은 100
        Inventory inventory = new InventoryTestBuilder().build();
        Inventory allocatedInventory = inventory.allocate(30);

        // when: 할당 재고 30개 중 20개를 할당 취소
        Inventory returnedInventory = allocatedInventory.unallocate(20);

        // then 1: 할당 재고 30개 중 20개를 취소했으므로 남은 할당 재고는 10개
        assertThat(allocatedInventory.getQuantity()).isEqualTo(10);
        assertThat(allocatedInventory.getStatusSet().allocStatus()).isEqualTo(AllocStatus.ALLOCATED);

        // then 2: 할당 취소 된 재고의 갯수는 20개
        assertThat(returnedInventory.getQuantity()).isEqualTo(20);
        assertThat(returnedInventory.getAvailableQuantity()).isEqualTo(20);
        assertThat(returnedInventory.getStatusSet().allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);
    }

    @Test
    void 이미_미할당_상태인_재고를_할당_취소_하려고_하면_예외를_던진다() {
        // given
        Inventory inventory = new InventoryTestBuilder().build();

        // when & then
        assertThatThrownBy(() ->
                inventory.unallocate(10)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(InventoryConstants.UNALLOCATE_FOR_ALLOCATED_ONLY_MESSAGE);
    }

    @Test
    void 일부_수량을_이동_시작하면_원래_재고의_수량과_가용_수량이_차감되고_MOVING_상태인_새_객체가_반환된다() {
        // given
        Inventory inventory = new InventoryTestBuilder().build();

        // when
        int movingQuantity = 40;
        Inventory movingInventory = inventory.startMoving(movingQuantity);

        // then 1: 원래 재고의 수량과 가용 수량이 차감된다
        assertThat(inventory.getQuantity()).isEqualTo(60);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(60);

        // then 2: 이동 중 상태인 새 객체를 반환한다.
        assertThat(movingInventory.getStatusSet().locStatus()).isEqualTo(LocStatus.MOVING);
        assertThat(movingInventory.getAvailableQuantity()).isEqualTo(movingQuantity);
    }

    @Test
    void 이동_중인_재고의_일부_수량이_도착하면_원래_재고의_수량은_차감되고_STORED_및_가용_수량이_복구된_새_객체가_반환된다() {
        // given
        Inventory inventory = new InventoryTestBuilder().build();
        Inventory movingInventory = inventory.startMoving(30);

        // when
        Inventory finishedInventory = movingInventory.finishMoving(10);

        // then 1: 이동 중인 재고의 수량이 30 - 10 이 된다
        assertThat(movingInventory.getQuantity()).isEqualTo(20);

        // then 2: 도착한 재고는 가용 수량이 10이면서 상태가 STORED인 새 반환 객체이다.
        assertThat(finishedInventory.getAvailableQuantity()).isEqualTo(10);
        assertThat(finishedInventory.getStatusSet().locStatus()).isEqualTo(LocStatus.STORED);
    }

    @Test
    void 일부_수량을_검수_시작하면_원래_재고의_가용_수량이_차감되고_INSPECTING_상태인_새_객체가_반환된다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .quantity(100)
                .availableQuantity(100)
                .build();

        // when
        Inventory inspectingInventory = inventory.startInspecting(30);

        // then 1: 원래 재고 검증
        assertThat(inventory.getQuantity()).isEqualTo(70);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(70);

        // then 2: 분할된 검수 재고 검증
        assertThat(inspectingInventory.getQuantity()).isEqualTo(30);
        assertThat(inspectingInventory.getAvailableQuantity()).isZero(); // 검수 중이므로 가용 수량 0
        assertThat(inspectingInventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.INSPECTING);
    }

    @Test
    void 전체_수량을_검수_시작하면_새_객체_생성_없이_자기_자신의_상태가_INSPECTING으로_바뀌고_가용_수량은_0이_된다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(50)
                .build();

        // when
        Inventory resultInventory = inventory.startInspecting(50);

        // then
        assertThat(resultInventory).isSameAs(inventory);
        assertThat(inventory.getQuantity()).isEqualTo(50);
        assertThat(inventory.getAvailableQuantity()).isZero();
        assertThat(inventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.INSPECTING);
    }

    @Test
    void 불량_또는_검수_중인_재고의_일부_수량을_정상으로_복구하면_원본은_유지되고_가용_수량이_살아난_정상_상태의_새_객체가_반환된다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(0)
                .qualityStatus(QualityStatus.INSPECTING)
                .build();

        // when: 20개 검수 통과 (정상 복구)
        Inventory restoredInventory = inventory.restoreToNormalQuality(20);

        // then 1: 원본 검수 재고는 30개로 차감
        assertThat(inventory.getQuantity()).isEqualTo(30);
        assertThat(inventory.getAvailableQuantity()).isZero();

        // then 2: 복구된 재고는 20개이며 가용 수량도 20으로 복구
        assertThat(restoredInventory.getQuantity()).isEqualTo(20);
        assertThat(restoredInventory.getAvailableQuantity()).isEqualTo(20);
        assertThat(restoredInventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.NORMAL);
    }

    @Test
    void 불량_또는_검수_중인_재고의_전체_수량을_정상으로_복구하면_새_객체_생성_없이_자기_자신의_상태가_NORMAL로_바뀌고_가용_수량이_복구된다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .quantity(40)
                .availableQuantity(0)
                .qualityStatus(QualityStatus.HOLD)
                .build();

        // when
        Inventory resultInventory = inventory.restoreToNormalQuality(40);

        // then
        assertThat(resultInventory).isSameAs(inventory);
        assertThat(inventory.getQuantity()).isEqualTo(40);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(40); // 가용 수량 전량 복구
        assertThat(inventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.NORMAL);
    }

    @Test
    void 이미_할당된_재고에_대해_검수를_시작하려고_하면_예외를_던진다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        // when & then
        assertThatThrownBy(() -> inventory.startInspecting(10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(InventoryConstants.START_INSPECTING_FOR_UNALLOCATED_ONLY_MESSAGE);
    }

    @Test
    void 이미_할당된_재고의_품질을_정상으로_복구하려고_하면_예외를_던진다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        // when & then
        assertThatThrownBy(() -> inventory.restoreToNormalQuality(10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(InventoryConstants.CHANGE_QUALITY_FOR_UNALLOCATED_ONLY_MESSAGE);
    }

    @Test
    void 이미_할당된_재고를_출고_금지_처리하려고_하면_예외를_던진다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        // when & then
        assertThatThrownBy(() -> inventory.holdForQualityIssue(10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(InventoryConstants.HOLD_FOR_UNALLOCATED_ONLY_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void 품질_상태_변경_시_요청_수량이_0_이하이면_예외를_던진다(int invalidQuantity) {
        // given
        Inventory inventory = new InventoryTestBuilder().build();

        // when & then
        assertThatThrownBy(() -> inventory.holdForQualityIssue(invalidQuantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.INVALID_QUANTITY_MESSAGE);
    }

    @Test
    void 품질_상태_변경_시_요청_수량이_현재_재고_수량을_초과하면_예외를_던진다() {
        // given
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(50)
                .build();

        // when & then
        assertThatThrownBy(() -> inventory.holdForQualityIssue(51))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.EXCEED_INVENTORY_QUANTITY_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void 위치_이동_변경_시_요청_수량이_0_이하이면_예외를_던진다(int invalidQuantity) {
        // given
        Inventory inventory = new InventoryTestBuilder().build();

        // when & then
        assertThatThrownBy(() -> inventory.startMoving(invalidQuantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.INVALID_QUANTITY_MESSAGE);
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
