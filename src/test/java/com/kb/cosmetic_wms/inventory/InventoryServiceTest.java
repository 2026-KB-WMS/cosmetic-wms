package com.kb.cosmetic_wms.inventory;

import com.kb.cosmetic_wms.inventory.application.port.in.*;
import com.kb.cosmetic_wms.inventory.application.port.out.InventoryPort;
import com.kb.cosmetic_wms.inventory.application.port.out.InventoryTransactionPort;
import com.kb.cosmetic_wms.inventory.application.service.InventoryService;
import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import com.kb.cosmetic_wms.inventory.domain.exception.InsufficientInventoryException;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryErrorCode;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryNotFoundException;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryStateTransitionException;
import com.kb.cosmetic_wms.inventory.domain.model.Inventory;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryStatusSet;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryTransaction;
import com.kb.cosmetic_wms.inventory.fixture.InventoryDtoBuilder;
import com.kb.cosmetic_wms.inventory.fixture.InventoryTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private InventoryPort inventoryPort;

    @Mock
    private InventoryTransactionPort inventoryTransactionPort;

    private Inventory defaultInventory;

    @BeforeEach
    void setUp() {
        defaultInventory = new InventoryTestBuilder().build();
        ReflectionTestUtils.setField(defaultInventory, "id", 1L);
    }

    // =========================================================
    // 재고 조회
    // =========================================================

    @Nested
    class 재고_조회 {

        @Test
        void 존재하는_ID로_조회하면_재고_상세정보를_반환한다() {
            given(inventoryPort.findById(1L)).willReturn(Optional.of(defaultInventory));

            InventoryResult result = inventoryService.findById(1L);

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

            assertThatThrownBy(() -> inventoryService.findById(999L))
                    .isInstanceOf(InventoryNotFoundException.class)
                    .hasMessage(InventoryErrorCode.INVENTORY_NOT_FOUND.getMessage());
        }

        @Test
        void LOT_ID로_조회하면_해당_LOT의_전체_재고_목록을_반환한다() {
            Inventory secondInventory = new InventoryTestBuilder().quantity(50).availableQuantity(50).build();
            ReflectionTestUtils.setField(secondInventory, "id", 2L);

            given(inventoryPort.findByLotId(10L)).willReturn(List.of(defaultInventory, secondInventory));

            List<InventoryResult> result = inventoryService.findByLotId(10L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(1).id()).isEqualTo(2L);
            assertThat(result.get(1).quantity()).isEqualTo(50);
        }

        @Test
        void 해당_LOT에_재고가_없으면_빈_목록을_반환한다() {
            given(inventoryPort.findByLotId(10L)).willReturn(List.of());

            List<InventoryResult> result = inventoryService.findByLotId(10L);

            assertThat(result).isEmpty();
        }

        @Test
        void 상품_ID로_조회하면_해당_상품의_전체_재고_목록을_반환한다() {
            given(inventoryPort.findByProductId(1L)).willReturn(List.of(defaultInventory));

            List<InventoryResult> result = inventoryService.findByProductId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(1L);
        }
    }

    // =========================================================
    // 출고 할당 (allocate)
    // =========================================================

    @Nested
    class 출고_할당 {

        @Test
        void 전체_수량을_할당하면_해당_재고가_ALLOCATED_상태로_변경되어_반환된다() {
            InventoryStatusChangeCommand command = new InventoryDtoBuilder()
                    .quantity(100).referenceId(10L).memberId(1L).build();

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.empty());

            InventoryResult result = inventoryService.allocate(1L, command);

            assertThat(result.allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
            assertThat(result.availableQuantity()).isEqualTo(0);
            verify(inventoryPort, never()).save(any());
        }

        @Test
        void 전체_수량을_할당할_때_동일_상태_재고가_이미_존재하면_원본을_삭제하고_수량을_합산한다() {
            InventoryStatusChangeCommand command = new InventoryDtoBuilder()
                    .quantity(100).referenceId(10L).memberId(1L).build();

            Inventory existingAllocated = new InventoryTestBuilder()
                    .quantity(50).availableQuantity(0)
                    .allocStatus(AllocStatus.ALLOCATED).build();
            ReflectionTestUtils.setField(existingAllocated, "id", 2L);

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.of(existingAllocated));

            InventoryResult result = inventoryService.allocate(1L, command);

            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.quantity()).isEqualTo(150);
            assertThat(result.allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
            verify(inventoryPort).delete(defaultInventory);
            verify(inventoryPort, never()).save(any());
        }

        @Test
        void 부분_수량을_할당하면_원본_수량이_감소하고_ALLOCATED_분할_재고가_저장되며_이력이_2건_기록된다() {
            InventoryStatusChangeCommand command = new InventoryDtoBuilder()
                    .quantity(30).referenceId(10L).memberId(1L).build();

            Inventory splitResult = new InventoryTestBuilder()
                    .quantity(30).availableQuantity(0)
                    .allocStatus(AllocStatus.ALLOCATED).build();
            ReflectionTestUtils.setField(splitResult, "id", 2L);

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.empty());
            given(inventoryPort.save(any(Inventory.class))).willReturn(splitResult);

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            InventoryResult result = inventoryService.allocate(1L, command);

            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.quantity()).isEqualTo(30);
            assertThat(result.allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
            assertThat(defaultInventory.getQuantity()).isEqualTo(70);
            verify(inventoryPort, times(1)).save(any(Inventory.class));

            verify(inventoryTransactionPort, times(2)).save(txCaptor.capture());
            List<InventoryTransaction> recorded = txCaptor.getAllValues();

            InventoryTransaction deductTx = recorded.get(0);
            assertThat(deductTx.getTransactionType()).isEqualTo(TransactionType.SPLIT_DEDUCT);
            assertThat(deductTx.getInventoryId()).isEqualTo(1L);
            assertThat(deductTx.getTransactionQuantity()).isEqualTo(30);
            assertThat(deductTx.getBalanceQuantity()).isEqualTo(70);

            InventoryTransaction allocTx = recorded.get(1);
            assertThat(allocTx.getTransactionType()).isEqualTo(TransactionType.ALLOCATE);
            assertThat(allocTx.getInventoryId()).isEqualTo(2L);
            assertThat(allocTx.getTransactionQuantity()).isEqualTo(30);
        }

        @Test
        void 부분_수량을_할당할_때_동일_상태_재고가_이미_존재하면_신규_저장_없이_수량이_합산되며_이력이_2건_기록된다() {
            InventoryStatusChangeCommand command = new InventoryDtoBuilder()
                    .quantity(30).referenceId(10L).memberId(1L).build();

            Inventory existingAllocated = new InventoryTestBuilder()
                    .quantity(20).availableQuantity(0)
                    .allocStatus(AllocStatus.ALLOCATED).build();
            ReflectionTestUtils.setField(existingAllocated, "id", 2L);

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.of(existingAllocated));

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            InventoryResult result = inventoryService.allocate(1L, command);

            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.quantity()).isEqualTo(50);
            assertThat(result.allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
            assertThat(defaultInventory.getQuantity()).isEqualTo(70);
            verify(inventoryPort, never()).save(any());

            verify(inventoryTransactionPort, times(2)).save(txCaptor.capture());
            List<InventoryTransaction> recorded = txCaptor.getAllValues();

            assertThat(recorded.get(0).getTransactionType()).isEqualTo(TransactionType.SPLIT_DEDUCT);
            assertThat(recorded.get(0).getInventoryId()).isEqualTo(1L);
            assertThat(recorded.get(1).getTransactionType()).isEqualTo(TransactionType.ALLOCATE);
            assertThat(recorded.get(1).getInventoryId()).isEqualTo(2L);
        }

        @Test
        void 할당_성공_시_ALLOCATE_트랜잭션_이력이_생성된다() {
            InventoryStatusChangeCommand command = new InventoryDtoBuilder()
                    .quantity(100).referenceId(10L).memberId(1L).build();

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.empty());

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            inventoryService.allocate(1L, command);

            verify(inventoryTransactionPort).save(txCaptor.capture());
            InventoryTransaction recorded = txCaptor.getValue();
            assertThat(recorded.getTransactionType()).isEqualTo(TransactionType.ALLOCATE);
            assertThat(recorded.getTransactionQuantity()).isEqualTo(100);
            assertThat(recorded.getCurrStatusSet().allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
        }

        @Test
        void 재고가_없으면_InventoryNotFoundException이_발생한다() {
            given(inventoryPort.findByIdForUpdate(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.allocate(999L,
                    new InventoryDtoBuilder().quantity(10).build()))
                    .isInstanceOf(InventoryNotFoundException.class);
        }

        @Test
        void 가용_수량을_초과하는_할당_요청은_InsufficientInventoryException이_전파된다() {
            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));

            assertThatThrownBy(() -> inventoryService.allocate(1L,
                    new InventoryDtoBuilder().quantity(200).referenceId(10L).build()))
                    .isInstanceOf(InsufficientInventoryException.class)
                    .hasMessage("가용 재고가 부족하여 할당할 수 없습니다.");
        }
    }

    // =========================================================
    // 할당 취소 (unallocate)
    // =========================================================

    @Nested
    class 할당_취소 {

        @Test
        void ALLOCATED_재고를_전체_취소하면_UNALLOCATED_상태로_복귀되고_이력이_기록된다() {
            Inventory allocated = new InventoryTestBuilder()
                    .quantity(50).availableQuantity(0)
                    .allocStatus(AllocStatus.ALLOCATED).build();
            ReflectionTestUtils.setField(allocated, "id", 1L);

            InventoryStatusChangeCommand command = new InventoryDtoBuilder()
                    .quantity(50).referenceId(10L).memberId(1L).build();

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(allocated));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.empty());

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            InventoryResult result = inventoryService.unallocate(1L, command);

            assertThat(result.allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);
            verify(inventoryTransactionPort).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.UNALLOCATE);
        }

        @Test
        void UNALLOCATED_재고에_할당_취소를_요청하면_InventoryStateTransitionException이_전파된다() {
            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));

            assertThatThrownBy(() -> inventoryService.unallocate(1L,
                    new InventoryDtoBuilder().quantity(50).build()))
                    .isInstanceOf(InventoryStateTransitionException.class)
                    .hasMessage("할당된 재고만 할당 취소할 수 있습니다.");
        }

        @Test
        void 재고가_없으면_InventoryNotFoundException이_발생한다() {
            given(inventoryPort.findByIdForUpdate(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.unallocate(999L,
                    new InventoryDtoBuilder().quantity(10).build()))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    // =========================================================
    // 위치 이동 (startMoving / finishMoving)
    // =========================================================

    @Nested
    class 위치_이동 {

        @Test
        void 미할당_STORED_재고를_MOVING_상태로_전환하면_성공하고_이력이_기록된다() {
            InventoryStatusChangeCommand command = new InventoryDtoBuilder()
                    .quantity(100).buildWithoutRef();

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.empty());
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            InventoryResult result = inventoryService.startMoving(1L, command);

            assertThat(result.locStatus()).isEqualTo(LocStatus.MOVING);
            verify(inventoryTransactionPort).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.LOCATION_MOVE);
        }

        @Test
        void MOVING_재고를_이동_완료하면_STORED_상태로_복귀하고_이력이_기록된다() {
            Inventory movingInventory = new InventoryTestBuilder()
                    .locStatus(LocStatus.MOVING).build();
            ReflectionTestUtils.setField(movingInventory, "id", 1L);

            InventoryStatusChangeCommand command = new InventoryDtoBuilder()
                    .quantity(100).buildWithoutRef();

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(movingInventory));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.empty());
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            InventoryResult result = inventoryService.finishMoving(1L, command);

            assertThat(result.locStatus()).isEqualTo(LocStatus.STORED);
            verify(inventoryTransactionPort).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.LOCATION_MOVE);
        }

        @Test
        void ALLOCATED_재고를_이동_시작하면_InventoryStateTransitionException이_전파된다() {
            Inventory allocated = new InventoryTestBuilder()
                    .allocStatus(AllocStatus.ALLOCATED).availableQuantity(0).build();
            ReflectionTestUtils.setField(allocated, "id", 1L);

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(allocated));

            assertThatThrownBy(() -> inventoryService.startMoving(1L,
                    new InventoryDtoBuilder().quantity(100).buildWithoutRef()))
                    .isInstanceOf(InventoryStateTransitionException.class)
                    .hasMessage("이미 할당된 재고는 이동(MOVING) 시킬 수 없습니다. 할당 취소부터 진행해주세요.");
        }

        @Test
        void 재고가_없으면_startMoving에서_InventoryNotFoundException이_발생한다() {
            given(inventoryPort.findByIdForUpdate(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.startMoving(999L,
                    new InventoryDtoBuilder().quantity(10).buildWithoutRef()))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    // =========================================================
    // 품질 상태 변경
    // =========================================================

    @Nested
    class 품질_상태_변경 {

        @Test
        void 정상_재고를_INSPECTING_상태로_변경하면_성공하고_이력이_기록된다() {
            InventoryStatusChangeCommand command = new InventoryDtoBuilder()
                    .quantity(100).buildWithoutRef();

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.empty());
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            InventoryResult result = inventoryService.startInspecting(1L, command);

            assertThat(result.qualityStatus()).isEqualTo(QualityStatus.INSPECTING);
            assertThat(result.availableQuantity()).isEqualTo(0);
            verify(inventoryTransactionPort).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.QUALITY_INSPECTING);
        }

        @Test
        void INSPECTING_재고를_정상으로_복귀하면_NORMAL_상태가_되고_이력이_기록된다() {
            Inventory inspecting = new InventoryTestBuilder()
                    .qualityStatus(QualityStatus.INSPECTING).availableQuantity(0).build();
            ReflectionTestUtils.setField(inspecting, "id", 1L);

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(inspecting));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.empty());
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            InventoryResult result = inventoryService.restoreToNormalQuality(1L,
                    new InventoryDtoBuilder().quantity(100).buildWithoutRef());

            assertThat(result.qualityStatus()).isEqualTo(QualityStatus.NORMAL);
            verify(inventoryTransactionPort).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.QUALITY_RELEASE);
        }

        @Test
        void 정상_재고를_HOLD_상태로_변경하면_출고_가능_수량이_0이_되고_이력이_기록된다() {
            InventoryStatusChangeCommand command = new InventoryDtoBuilder()
                    .quantity(100).buildWithoutRef();

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.empty());
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            InventoryResult result = inventoryService.holdForQualityIssue(1L, command);

            assertThat(result.qualityStatus()).isEqualTo(QualityStatus.HOLD);
            assertThat(result.availableQuantity()).isEqualTo(0);
            verify(inventoryTransactionPort).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.QUALITY_HOLD);
        }

        @Test
        void 정상_재고를_폐기_예정으로_변경하면_DISCARD_SCHEDULED_상태가_되고_이력이_기록된다() {
            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(defaultInventory));
            given(inventoryPort.findMergeTargetForUpdate(any(), any(), any(), any(), any()))
                    .willReturn(Optional.empty());
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            InventoryResult result = inventoryService.scheduleForDiscard(1L,
                    new InventoryDtoBuilder().quantity(100).buildWithoutRef());

            assertThat(result.qualityStatus()).isEqualTo(QualityStatus.DISCARD_SCHEDULED);
            verify(inventoryTransactionPort).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.DISCARD);
        }

        @Test
        void 할당된_재고에_품질_HOLD를_요청하면_InventoryStateTransitionException이_전파된다() {
            Inventory allocated = new InventoryTestBuilder()
                    .allocStatus(AllocStatus.ALLOCATED).availableQuantity(0).build();
            ReflectionTestUtils.setField(allocated, "id", 1L);

            given(inventoryPort.findByIdForUpdate(1L)).willReturn(Optional.of(allocated));

            assertThatThrownBy(() -> inventoryService.holdForQualityIssue(1L,
                    new InventoryDtoBuilder().quantity(100).buildWithoutRef()))
                    .isInstanceOf(InventoryStateTransitionException.class)
                    .hasMessage("이미 할당된 재고는 출고 금지(HOLD) 처리할 수 없습니다. 할당 취소부터 진행해주세요.");
        }

        @Test
        void 재고가_없으면_품질_상태_변경에서_InventoryNotFoundException이_발생한다() {
            given(inventoryPort.findByIdForUpdate(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.startInspecting(999L,
                    new InventoryDtoBuilder().quantity(10).buildWithoutRef()))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    // =========================================================
    // 입고 반영 (createFromInbound)
    // =========================================================

    @Nested
    class 입고_반영 {

        private static final Long PRODUCT_ID = 1L;
        private static final Long LOT_ID = 10L;
        private static final Long SECTION_ID = 1000L;
        private static final Long WAREHOUSE_ID = 100L;
        private static final Long INBOUND_ID = 99L;
        private static final Long MEMBER_ID = 1L;

        private final InventoryStatusSet putawayStatus = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, QualityStatus.NORMAL, LocStatus.STORED
        );

        @Test
        void 동일_복합키_재고가_없으면_새_재고가_생성되고_INBOUND_PUTAWAY_이력이_기록된다() {
            Inventory saved = new InventoryTestBuilder().quantity(50).availableQuantity(50).build();
            ReflectionTestUtils.setField(saved, "id", 10L);

            given(inventoryPort.findMergeTargetForUpdate(PRODUCT_ID, LOT_ID, SECTION_ID, putawayStatus, -1L))
                    .willReturn(Optional.empty());
            given(inventoryPort.save(any(Inventory.class))).willReturn(saved);

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            inventoryService.createFromInbound(new InboundPutawayCommand(PRODUCT_ID, LOT_ID, SECTION_ID, WAREHOUSE_ID, 50, INBOUND_ID, MEMBER_ID, LocalDate.of(2026, 12, 31)));

            verify(inventoryPort).save(any(Inventory.class));
            verify(inventoryTransactionPort).save(txCaptor.capture());

            InventoryTransaction tx = txCaptor.getValue();
            assertThat(tx.getInventoryId()).isEqualTo(10L);
            assertThat(tx.getTransactionType()).isEqualTo(TransactionType.INBOUND_PUTAWAY);
            assertThat(tx.getTransactionQuantity()).isEqualTo(50);
            assertThat(tx.getBalanceQuantity()).isEqualTo(50);
            assertThat(tx.getReferenceId()).isEqualTo(INBOUND_ID);
            assertThat(tx.getPrevStatusSet()).isNull();
            assertThat(tx.getCurrStatusSet()).isEqualTo(putawayStatus);
        }

        @Test
        void 동일_복합키_재고가_이미_존재하면_수량이_합산되고_새_재고는_생성되지_않는다() {
            Inventory existing = new InventoryTestBuilder().quantity(100).availableQuantity(100).build();
            ReflectionTestUtils.setField(existing, "id", 5L);

            given(inventoryPort.findMergeTargetForUpdate(PRODUCT_ID, LOT_ID, SECTION_ID, putawayStatus, -1L))
                    .willReturn(Optional.of(existing));

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            inventoryService.createFromInbound(new InboundPutawayCommand(PRODUCT_ID, LOT_ID, SECTION_ID, WAREHOUSE_ID, 50, INBOUND_ID, MEMBER_ID, LocalDate.of(2026, 12, 31)));

            verify(inventoryPort, never()).save(any(Inventory.class));
            assertThat(existing.getQuantity()).isEqualTo(150);
            assertThat(existing.getAvailableQuantity()).isEqualTo(150);

            verify(inventoryTransactionPort).save(txCaptor.capture());
            InventoryTransaction tx = txCaptor.getValue();
            assertThat(tx.getInventoryId()).isEqualTo(5L);
            assertThat(tx.getTransactionType()).isEqualTo(TransactionType.INBOUND_PUTAWAY);
            assertThat(tx.getTransactionQuantity()).isEqualTo(50);
            assertThat(tx.getBalanceQuantity()).isEqualTo(150);
            assertThat(tx.getReferenceId()).isEqualTo(INBOUND_ID);
            assertThat(tx.getPrevStatusSet()).isEqualTo(putawayStatus);
            assertThat(tx.getCurrStatusSet()).isEqualTo(putawayStatus);
        }

        @Test
        void 같은_입고_내_동일_복합키_품목이_두_건이면_두_번째_호출에서_수량이_합산된다() {
            Inventory firstSaved = new InventoryTestBuilder().quantity(60).availableQuantity(60).build();
            ReflectionTestUtils.setField(firstSaved, "id", 7L);
            given(inventoryPort.save(any(Inventory.class))).willReturn(firstSaved);

            given(inventoryPort.findMergeTargetForUpdate(PRODUCT_ID, LOT_ID, SECTION_ID, putawayStatus, -1L))
                    .willReturn(Optional.empty())
                    .willReturn(Optional.of(firstSaved));

            inventoryService.createFromInbound(new InboundPutawayCommand(PRODUCT_ID, LOT_ID, SECTION_ID, WAREHOUSE_ID, 60, INBOUND_ID, MEMBER_ID, LocalDate.of(2026, 12, 31)));
            inventoryService.createFromInbound(new InboundPutawayCommand(PRODUCT_ID, LOT_ID, SECTION_ID, WAREHOUSE_ID, 40, INBOUND_ID, MEMBER_ID, LocalDate.of(2026, 12, 31)));

            verify(inventoryPort, times(1)).save(any(Inventory.class));
            assertThat(firstSaved.getQuantity()).isEqualTo(100);
            assertThat(firstSaved.getAvailableQuantity()).isEqualTo(100);

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);
            verify(inventoryTransactionPort, times(2)).save(txCaptor.capture());

            InventoryTransaction mergeTx = txCaptor.getAllValues().get(1);
            assertThat(mergeTx.getTransactionQuantity()).isEqualTo(40);
            assertThat(mergeTx.getBalanceQuantity()).isEqualTo(100);
        }
    }

    // =========================================================
    // 검사 결과 반영 (applyInspectionResult)
    // =========================================================

    @Nested
    class 검사_결과_반영 {

        private static final Long PRODUCT_ID = 1L;
        private static final Long LOT_ID = 10L;
        private static final Long STORAGE_SECTION_ID = 1001L;
        private static final Long QUARANTINE_SECTION_ID = 1002L;
        private static final Long WAREHOUSE_ID = 100L;
        private static final Long INSPECTION_ID = 55L;
        private static final Long MEMBER_ID = 1L;

        private final InventoryStatusSet passStatus = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, QualityStatus.NORMAL, LocStatus.STORED
        );
        private final InventoryStatusSet holdStatus = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, QualityStatus.HOLD, LocStatus.STORED
        );

        @Test
        void 합격_수량이_있으면_NORMAL_STORED_상태_재고가_생성되고_출고_가능_수량은_합격_수량과_동일하다() {
            Inventory saved = new InventoryTestBuilder()
                    .locStatus(LocStatus.STORED).qualityStatus(QualityStatus.NORMAL)
                    .quantity(80).availableQuantity(80).build();
            ReflectionTestUtils.setField(saved, "id", 20L);

            given(inventoryPort.findMergeTargetForUpdate(PRODUCT_ID, LOT_ID, STORAGE_SECTION_ID, passStatus, -1L))
                    .willReturn(Optional.empty());
            given(inventoryPort.save(any(Inventory.class))).willReturn(saved);

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            inventoryService.applyInspectionResult(new InspectionResultCommand(
                    PRODUCT_ID, LOT_ID, STORAGE_SECTION_ID, null, WAREHOUSE_ID, 80, 0, INSPECTION_ID, MEMBER_ID, LocalDate.of(2026, 12, 31)));

            verify(inventoryPort).save(any(Inventory.class));
            verify(inventoryTransactionPort).save(txCaptor.capture());

            InventoryTransaction tx = txCaptor.getValue();
            assertThat(tx.getTransactionType()).isEqualTo(TransactionType.INSPECTION_PASS);
            assertThat(tx.getCurrStatusSet()).isEqualTo(passStatus);
            assertThat(saved.getAvailableQuantity()).isEqualTo(80);
        }

        @Test
        void 불합격_수량이_있으면_HOLD_STORED_상태_재고가_생성되고_출고_가능_수량은_0이다() {
            Inventory saved = new InventoryTestBuilder()
                    .locStatus(LocStatus.STORED).qualityStatus(QualityStatus.HOLD)
                    .quantity(20).availableQuantity(0).build();
            ReflectionTestUtils.setField(saved, "id", 21L);

            given(inventoryPort.findMergeTargetForUpdate(PRODUCT_ID, LOT_ID, QUARANTINE_SECTION_ID, holdStatus, -1L))
                    .willReturn(Optional.empty());
            given(inventoryPort.save(any(Inventory.class))).willReturn(saved);

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            inventoryService.applyInspectionResult(new InspectionResultCommand(
                    PRODUCT_ID, LOT_ID, null, QUARANTINE_SECTION_ID, WAREHOUSE_ID, 0, 20, INSPECTION_ID, MEMBER_ID, LocalDate.of(2026, 12, 31)));

            verify(inventoryTransactionPort).save(txCaptor.capture());
            InventoryTransaction tx = txCaptor.getValue();
            assertThat(tx.getTransactionType()).isEqualTo(TransactionType.INSPECTION_FAIL);
            assertThat(tx.getCurrStatusSet()).isEqualTo(holdStatus);
        }
    }
}