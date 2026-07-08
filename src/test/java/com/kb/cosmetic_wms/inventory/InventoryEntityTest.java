package com.kb.cosmetic_wms.inventory;

import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.exception.InsufficientInventoryException;
import com.kb.cosmetic_wms.inventory.domain.exception.InvalidInventoryQuantityException;
import com.kb.cosmetic_wms.inventory.domain.exception.InvalidInventoryStatusCombinationException;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryMergeException;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryStateTransitionException;
import com.kb.cosmetic_wms.inventory.domain.model.Inventory;
import com.kb.cosmetic_wms.inventory.domain.model.SplitResult;
import com.kb.cosmetic_wms.inventory.fixture.InventoryTestBuilder;
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
                .isInstanceOf(InvalidInventoryQuantityException.class)
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
                .isInstanceOf(InvalidInventoryQuantityException.class)
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
                .isInstanceOf(InvalidInventoryQuantityException.class)
                .hasMessageContaining("출고 가능 수량은 0이어야 합니다.");
    }

    @Test
    void 정상적인_재고에서_일부_수량을_할당하면_원래_재고는_수량이_깎이고_할당된_새로운_재고_객체가_반환된다() {
        Inventory originalInventory = new InventoryTestBuilder()
                .quantity(100)
                .availableQuantity(100)
                .allocStatus(AllocStatus.UNALLOCATED)
                .build();

        SplitResult splitResult = originalInventory.allocate(40);
        Inventory allocatedInventory = splitResult.result();

        assertThat(originalInventory.getQuantity()).isEqualTo(60);
        assertThat(originalInventory.getAvailableQuantity()).isEqualTo(60);
        assertThat(originalInventory.getStatusSet().allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);

        assertThat(allocatedInventory.getQuantity()).isEqualTo(40);
        assertThat(allocatedInventory.getAvailableQuantity()).isEqualTo(0);
        assertThat(allocatedInventory.getStatusSet().allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
        assertThat(splitResult.wasSplit()).isTrue();
    }

    @Test
    void 원래_재고의_가용_수량을_정확히_전부_할당하면_새_객체를_만들지_않고_자신이_ALLOCATED로_변환된다() {
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(50)
                .allocStatus(AllocStatus.UNALLOCATED)
                .build();

        SplitResult splitResult = inventory.allocate(50);

        assertThat(splitResult.wasSplit()).isFalse();
        assertThat(splitResult.result()).isSameAs(inventory);
        assertThat(inventory.getQuantity()).isEqualTo(50);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(0);
        assertThat(inventory.getStatusSet().allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
    }

    @Test
    void 가용_재고를_초과하여_할당을_시도하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(50)
                .build();

        assertThatThrownBy(() ->
                inventory.allocate(60)
        )
                .isInstanceOf(InsufficientInventoryException.class)
                .hasMessage("가용 재고가 부족하여 할당할 수 없습니다.");
    }

    @Test
    void 품질_상태가_불량으로_변할_때_원본_재고의_가용_수량이_차감되고_새_재고의_가용_수량은_0이어야_한다() {
        Inventory inventory = new InventoryTestBuilder().build();
        int targetQuantity = 30;

        SplitResult splitResult = inventory.holdForQualityIssue(targetQuantity);
        Inventory brokenInventory = splitResult.result();

        assertThat(inventory.getAvailableQuantity()).isEqualTo(70);
        assertThat(inventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.NORMAL);

        assertThat(brokenInventory.getQuantity()).isEqualTo(targetQuantity);
        assertThat(brokenInventory.getAvailableQuantity()).isZero();
        assertThat(brokenInventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.HOLD);
        assertThat(splitResult.wasSplit()).isTrue();
    }

    @Test
    void 전체_수량의_품질_상태가_불량으로_변할_때는_객체_분할_없이_자기_자신의_가용_수량이_0이_되어야_한다() {
        int totalQuantity = 50;
        Inventory inventory = new InventoryTestBuilder()
                .quantity(totalQuantity)
                .availableQuantity(totalQuantity)
                .build();

        SplitResult splitResult = inventory.holdForQualityIssue(totalQuantity);

        assertThat(splitResult.wasSplit()).isFalse();
        assertThat(splitResult.result()).isSameAs(inventory);
        assertThat(inventory.getQuantity()).isEqualTo(totalQuantity);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(0);
        assertThat(inventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.HOLD);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10, -100})
    void 할당_수량에_0_이하의_값이_들어오면_예외를_던진다(int invalidQuantity) {
        Inventory inventory = new InventoryTestBuilder().build();

        assertThatThrownBy(() ->
                inventory.allocate(invalidQuantity)
        )
                .isInstanceOf(InvalidInventoryQuantityException.class)
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
                .isInstanceOf(InvalidInventoryStatusCombinationException.class)
                .hasMessageContaining(expectedMsg);
    }

    @Test
    void 이미_할당된_재고의_품질을_폐기_예정으로_변경하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        assertThatThrownBy(() ->
                inventory.scheduleForDiscard(10)
        )
                .isInstanceOf(InventoryStateTransitionException.class)
                .hasMessage("할당된 재고는 폐기 처리할 수 없습니다. 할당 취소부터 진행해주세요.");
    }

    @Test
    void 이미_할당된_재고를_창고_간_이동_상태로_변경하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        Assertions.assertThatThrownBy(() ->
                        inventory.startMoving(20)
                )
                .isInstanceOf(InventoryStateTransitionException.class)
                .hasMessageContaining("이미 할당된 재고는 이동(MOVING) 시킬 수 없습니다.");
    }

    @Test
    void 일부_수량을_할당_취소하면_원래_재고의_수량은_유지되고_가용_수량이_복구된_새_객체가_반환된다() {
        Inventory inventory = new InventoryTestBuilder().build();
        Inventory allocatedInventory = inventory.allocate(30).result();

        SplitResult unallocSplit = allocatedInventory.unallocate(20);
        Inventory returnedInventory = unallocSplit.result();

        assertThat(allocatedInventory.getQuantity()).isEqualTo(10);
        assertThat(allocatedInventory.getStatusSet().allocStatus()).isEqualTo(AllocStatus.ALLOCATED);

        assertThat(returnedInventory.getQuantity()).isEqualTo(20);
        assertThat(returnedInventory.getAvailableQuantity()).isEqualTo(20);
        assertThat(returnedInventory.getStatusSet().allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);
    }

    @Test
    void 이미_미할당_상태인_재고를_할당_취소_하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder().build();

        assertThatThrownBy(() ->
                inventory.unallocate(10)
        )
                .isInstanceOf(InventoryStateTransitionException.class)
                .hasMessage("할당된 재고만 할당 취소할 수 있습니다.");
    }

    @Test
    void 일부_수량을_이동_시작하면_원래_재고의_수량과_가용_수량이_차감되고_MOVING_상태인_새_객체가_반환된다() {
        Inventory inventory = new InventoryTestBuilder().build();

        int movingQuantity = 40;
        SplitResult splitResult = inventory.startMoving(movingQuantity);
        Inventory movingInventory = splitResult.result();

        assertThat(inventory.getQuantity()).isEqualTo(60);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(60);

        assertThat(movingInventory.getStatusSet().locStatus()).isEqualTo(LocStatus.MOVING);
        assertThat(movingInventory.getAvailableQuantity()).isEqualTo(movingQuantity);
        assertThat(splitResult.wasSplit()).isTrue();
    }

    @Test
    void 이미_이동_중인_재고를_중복해서_이동_시작하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .locStatus(LocStatus.MOVING)
                .build();

        assertThatThrownBy(() -> inventory.startMoving(10))
                .isInstanceOf(InventoryStateTransitionException.class)
                .hasMessage("이미 이동 중인 재고입니다.");
    }

    @Test
    void 이동_중인_재고의_일부_수량이_도착하면_원래_재고의_수량은_차감되고_STORED_및_가용_수량이_복구된_새_객체가_반환된다() {
        Inventory inventory = new InventoryTestBuilder().build();
        Inventory movingInventory = inventory.startMoving(30).result();

        SplitResult finishSplit = movingInventory.finishMoving(10);
        Inventory finishedInventory = finishSplit.result();

        assertThat(movingInventory.getQuantity()).isEqualTo(20);

        assertThat(finishedInventory.getAvailableQuantity()).isEqualTo(10);
        assertThat(finishedInventory.getStatusSet().locStatus()).isEqualTo(LocStatus.STORED);
    }

    @Test
    void 이동_중_상태가_아닌_재고를_이동_완료_처리하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .locStatus(LocStatus.STORED)
                .build();

        assertThatThrownBy(() -> inventory.finishMoving(10))
                .isInstanceOf(InventoryStateTransitionException.class)
                .hasMessage("이동 중(MOVING) 상태의 재고만 이동 완료 처리가 가능합니다.");
    }

    @Test
    void 일부_수량을_검수_시작하면_원래_재고의_가용_수량이_차감되고_INSPECTING_상태인_새_객체가_반환된다() {
        Inventory inventory = new InventoryTestBuilder()
                .quantity(100)
                .availableQuantity(100)
                .build();

        SplitResult splitResult = inventory.startInspecting(30);
        Inventory inspectingInventory = splitResult.result();

        assertThat(inventory.getQuantity()).isEqualTo(70);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(70);

        assertThat(inspectingInventory.getQuantity()).isEqualTo(30);
        assertThat(inspectingInventory.getAvailableQuantity()).isZero();
        assertThat(inspectingInventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.INSPECTING);
        assertThat(splitResult.wasSplit()).isTrue();
    }

    @Test
    void 전체_수량을_검수_시작하면_새_객체_생성_없이_자기_자신의_상태가_INSPECTING으로_바뀌고_가용_수량은_0이_된다() {
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(50)
                .build();

        SplitResult splitResult = inventory.startInspecting(50);

        assertThat(splitResult.wasSplit()).isFalse();
        assertThat(splitResult.result()).isSameAs(inventory);
        assertThat(inventory.getQuantity()).isEqualTo(50);
        assertThat(inventory.getAvailableQuantity()).isZero();
        assertThat(inventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.INSPECTING);
    }

    @Test
    void 불량_또는_검수_중인_재고의_일부_수량을_정상으로_복구하면_원본은_유지되고_가용_수량이_살아난_정상_상태의_새_객체가_반환된다() {
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(0)
                .qualityStatus(QualityStatus.INSPECTING)
                .build();

        SplitResult splitResult = inventory.restoreToNormalQuality(20);
        Inventory restoredInventory = splitResult.result();

        assertThat(inventory.getQuantity()).isEqualTo(30);
        assertThat(inventory.getAvailableQuantity()).isZero();

        assertThat(restoredInventory.getQuantity()).isEqualTo(20);
        assertThat(restoredInventory.getAvailableQuantity()).isEqualTo(20);
        assertThat(restoredInventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.NORMAL);
        assertThat(splitResult.wasSplit()).isTrue();
    }

    @Test
    void 불량_또는_검수_중인_재고의_전체_수량을_정상으로_복구하면_새_객체_생성_없이_자기_자신의_상태가_NORMAL로_바뀌고_가용_수량이_복구된다() {
        Inventory inventory = new InventoryTestBuilder()
                .quantity(40)
                .availableQuantity(0)
                .qualityStatus(QualityStatus.HOLD)
                .build();

        SplitResult splitResult = inventory.restoreToNormalQuality(40);

        assertThat(splitResult.wasSplit()).isFalse();
        assertThat(splitResult.result()).isSameAs(inventory);
        assertThat(inventory.getQuantity()).isEqualTo(40);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(40);
        assertThat(inventory.getStatusSet().qualityStatus()).isEqualTo(QualityStatus.NORMAL);
    }

    @Test
    void 이미_할당된_재고에_대해_검수를_시작하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        assertThatThrownBy(() -> inventory.startInspecting(10))
                .isInstanceOf(InventoryStateTransitionException.class)
                .hasMessage("이미 할당된 재고는 검수(INSPECTING) 상태로 변경할 수 없습니다. 할당 취소부터 진행해주세요.");
    }

    @Test
    void 이미_할당된_재고의_품질을_정상으로_복구하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        assertThatThrownBy(() -> inventory.restoreToNormalQuality(10))
                .isInstanceOf(InventoryStateTransitionException.class)
                .hasMessage("할당된 재고는 품질 상태를 변경할 수 없습니다.");
    }

    @Test
    void 이미_할당된_재고를_출고_금지_처리하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        assertThatThrownBy(() -> inventory.holdForQualityIssue(10))
                .isInstanceOf(InventoryStateTransitionException.class)
                .hasMessage("이미 할당된 재고는 출고 금지(HOLD) 처리할 수 없습니다. 할당 취소부터 진행해주세요.");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void 품질_상태_변경_시_요청_수량이_0_이하이면_예외를_던진다(int invalidQuantity) {
        Inventory inventory = new InventoryTestBuilder().build();

        assertThatThrownBy(() -> inventory.holdForQualityIssue(invalidQuantity))
                .isInstanceOf(InvalidInventoryQuantityException.class)
                .hasMessage("재고 수량은 음수일 수 없습니다.");
    }

    @Test
    void 품질_상태_변경_시_요청_수량이_현재_재고_수량을_초과하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(50)
                .build();

        assertThatThrownBy(() -> inventory.holdForQualityIssue(51))
                .isInstanceOf(InvalidInventoryQuantityException.class)
                .hasMessage("변경 요청 수량이 현재 보유한 재고 수량을 초과할 수 없습니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void 위치_이동_변경_시_요청_수량이_0_이하이면_예외를_던진다(int invalidQuantity) {
        Inventory inventory = new InventoryTestBuilder().build();

        assertThatThrownBy(() -> inventory.startMoving(invalidQuantity))
                .isInstanceOf(InvalidInventoryQuantityException.class)
                .hasMessage("재고 수량은 음수일 수 없습니다.");
    }

    @Test
    void 도킹_구역_대기_재고를_생성할_때_출고_가능_수량이_0보다_크면_예외를_던진다() {
        assertThatThrownBy(() ->
                new InventoryTestBuilder()
                        .locStatus(LocStatus.DOCKING)
                        .availableQuantity(10)
                        .build()
        )
                .isInstanceOf(InvalidInventoryQuantityException.class)
                .hasMessage("도킹 구역 대기 중인 재고의 출고 가능 수량은 0이어야 합니다.");
    }

    @Test
    void 도킹_구역_대기_재고를_이동_시작하려고_하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .locStatus(LocStatus.DOCKING)
                .availableQuantity(0)
                .build();

        assertThatThrownBy(() -> inventory.startMoving(10))
                .isInstanceOf(InventoryStateTransitionException.class)
                .hasMessage("도킹 구역 대기 중인 재고는 이동(MOVING) 상태로 전환할 수 없습니다.");
    }

    @Test
    void 보유_수량_일부를_차감하면_잔여_수량이_감소하고_비어있지_않다() {
        Inventory inventory = new InventoryTestBuilder()
                .quantity(50)
                .availableQuantity(0)
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        inventory.deduct(30);

        assertThat(inventory.getQuantity()).isEqualTo(20);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(0);
        assertThat(inventory.isEmpty()).isFalse();
    }

    @Test
    void 보유_수량_전부를_차감하면_수량이_0이_되고_비어있다() {
        Inventory inventory = new InventoryTestBuilder()
                .quantity(30)
                .availableQuantity(0)
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        inventory.deduct(30);

        assertThat(inventory.getQuantity()).isZero();
        assertThat(inventory.isEmpty()).isTrue();
    }

    @Test
    void 보유_수량을_초과하여_차감을_시도하면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder()
                .quantity(30)
                .availableQuantity(0)
                .allocStatus(AllocStatus.ALLOCATED)
                .build();

        assertThatThrownBy(() -> inventory.deduct(40))
                .isInstanceOf(InvalidInventoryQuantityException.class);
    }

    @Test
    void 차감_수량이_0_이하이면_예외를_던진다() {
        Inventory inventory = new InventoryTestBuilder().build();

        assertThatThrownBy(() -> inventory.deduct(0))
                .isInstanceOf(InvalidInventoryQuantityException.class);
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
                        "품질 상태가 검수 대기/중인 결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다."),
                Arguments.of(AllocStatus.UNALLOCATED, QualityStatus.HOLD, LocStatus.MOVING,
                        "품질 상태가 출고 금지인 결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다."),

                Arguments.of(AllocStatus.ALLOCATED, QualityStatus.NORMAL, LocStatus.DOCKING,
                        "도킹 구역 대기 중인 재고는 할당 대상이 아닙니다."),
                Arguments.of(AllocStatus.SHIPPED, QualityStatus.NORMAL, LocStatus.DOCKING,
                        "도킹 구역 대기 중인 재고는 할당 대상이 아닙니다.")
        );
    }
}
