package com.kb.cosmetic_wms.inventory;

import com.kb.cosmetic_wms.inventory.application.port.in.*;
import com.kb.cosmetic_wms.inventory.application.port.out.InventoryPort;
import com.kb.cosmetic_wms.inventory.application.port.out.InventoryTransactionPort;
import com.kb.cosmetic_wms.inventory.application.port.out.SectionAssignmentPort;
import com.kb.cosmetic_wms.inventory.application.service.InventoryCommandService;
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
class InventoryCommandServiceTest {

    @InjectMocks
    private InventoryCommandService inventoryService;

    @Mock
    private InventoryPort inventoryPort;

    @Mock
    private InventoryTransactionPort inventoryTransactionPort;

    @Mock
    private SectionAssignmentPort sectionAssignmentPort;

    private Inventory defaultInventory;

    @BeforeEach
    void setUp() {
        defaultInventory = new InventoryTestBuilder().build();
        ReflectionTestUtils.setField(defaultInventory, "id", 1L);
        lenient().when(inventoryPort.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
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
            verify(inventoryPort).save(defaultInventory);
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
            verify(inventoryPort).save(existingAllocated);
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
            verify(inventoryPort, times(2)).save(any(Inventory.class));
            verify(inventoryPort).save(defaultInventory);

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
        void 부분_수량을_할당할_때_동일_상태_재고가_이미_존재하면_신규_생성_없이_기존_재고에_합산되며_이력이_2건_기록된다() {
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
            verify(inventoryPort).save(defaultInventory);
            verify(inventoryPort).save(existingAllocated);

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

            given(sectionAssignmentPort.assignSectionsForInspection(WAREHOUSE_ID, PRODUCT_ID, 80, 0))
                    .willReturn(new SectionAssignmentPort.SectionAssignment(STORAGE_SECTION_ID, null));
            given(inventoryPort.findMergeTargetForUpdate(PRODUCT_ID, LOT_ID, STORAGE_SECTION_ID, passStatus, -1L))
                    .willReturn(Optional.empty());
            given(inventoryPort.save(any(Inventory.class))).willReturn(saved);

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            inventoryService.applyInspectionResult(new InspectionResultCommand(
                    PRODUCT_ID, LOT_ID, WAREHOUSE_ID, 80, 0, INSPECTION_ID, MEMBER_ID, LocalDate.of(2026, 12, 31)));

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

            given(sectionAssignmentPort.assignSectionsForInspection(WAREHOUSE_ID, PRODUCT_ID, 0, 20))
                    .willReturn(new SectionAssignmentPort.SectionAssignment(null, QUARANTINE_SECTION_ID));
            given(inventoryPort.findMergeTargetForUpdate(PRODUCT_ID, LOT_ID, QUARANTINE_SECTION_ID, holdStatus, -1L))
                    .willReturn(Optional.empty());
            given(inventoryPort.save(any(Inventory.class))).willReturn(saved);

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            inventoryService.applyInspectionResult(new InspectionResultCommand(
                    PRODUCT_ID, LOT_ID, WAREHOUSE_ID, 0, 20, INSPECTION_ID, MEMBER_ID, LocalDate.of(2026, 12, 31)));

            verify(inventoryTransactionPort).save(txCaptor.capture());
            InventoryTransaction tx = txCaptor.getValue();
            assertThat(tx.getTransactionType()).isEqualTo(TransactionType.INSPECTION_FAIL);
            assertThat(tx.getCurrStatusSet()).isEqualTo(holdStatus);
        }
    }

    // =========================================================
    // 출고 차감 (deductForOutbound)
    // =========================================================

    @Nested
    class 출고_차감 {

        private static final Long OUTBOUND_ID = 10L;
        private static final Long MEMBER_ID = 1L;

        private final InventoryStatusSet allocatedStatus = InventoryStatusSet.of(
                AllocStatus.ALLOCATED, QualityStatus.NORMAL, LocStatus.STORED
        );

        private InventoryTransaction allocateTx(Long inventoryId, int quantity, int balance) {
            return InventoryTransaction.create(
                    inventoryId, TransactionType.ALLOCATE, quantity, balance,
                    OUTBOUND_ID, allocatedStatus, allocatedStatus, MEMBER_ID, null
            );
        }

        @Test
        void 병합으로_수량이_불어난_재고_행은_할당_수량만큼만_차감되고_삭제되지_않는다() {
            Inventory allocated = new InventoryTestBuilder()
                    .quantity(50).availableQuantity(0)
                    .allocStatus(AllocStatus.ALLOCATED).build();
            ReflectionTestUtils.setField(allocated, "id", 2L);

            given(inventoryTransactionPort.findByTransactionTypeAndReferenceId(TransactionType.ALLOCATE, OUTBOUND_ID))
                    .willReturn(List.of(allocateTx(2L, 30, 50)));
            given(inventoryPort.findByIdForUpdate(2L)).willReturn(Optional.of(allocated));

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            inventoryService.deductForOutbound(OUTBOUND_ID, MEMBER_ID);

            assertThat(allocated.getQuantity()).isEqualTo(20);
            verify(inventoryPort).save(allocated);
            verify(inventoryPort, never()).delete(any());

            verify(inventoryTransactionPort).save(txCaptor.capture());
            InventoryTransaction shipTx = txCaptor.getValue();
            assertThat(shipTx.getTransactionType()).isEqualTo(TransactionType.SHIP);
            assertThat(shipTx.getTransactionQuantity()).isEqualTo(30);
            assertThat(shipTx.getBalanceQuantity()).isEqualTo(20);
        }

        @Test
        void 차감_후_잔여_수량이_0이면_재고_행을_삭제한다() {
            Inventory allocated = new InventoryTestBuilder()
                    .quantity(30).availableQuantity(0)
                    .allocStatus(AllocStatus.ALLOCATED).build();
            ReflectionTestUtils.setField(allocated, "id", 2L);

            given(inventoryTransactionPort.findByTransactionTypeAndReferenceId(TransactionType.ALLOCATE, OUTBOUND_ID))
                    .willReturn(List.of(allocateTx(2L, 30, 30)));
            given(inventoryPort.findByIdForUpdate(2L)).willReturn(Optional.of(allocated));

            inventoryService.deductForOutbound(OUTBOUND_ID, MEMBER_ID);

            verify(inventoryPort).delete(allocated);
            verify(inventoryPort, never()).save(any(Inventory.class));
        }

        @Test
        void 같은_출고의_할당_이력이_여러_건이면_각_할당_수량만큼_순차_차감한다() {
            Inventory allocated = new InventoryTestBuilder()
                    .quantity(50).availableQuantity(0)
                    .allocStatus(AllocStatus.ALLOCATED).build();
            ReflectionTestUtils.setField(allocated, "id", 2L);

            given(inventoryTransactionPort.findByTransactionTypeAndReferenceId(TransactionType.ALLOCATE, OUTBOUND_ID))
                    .willReturn(List.of(allocateTx(2L, 30, 30), allocateTx(2L, 20, 50)));
            given(inventoryPort.findByIdForUpdate(2L)).willReturn(Optional.of(allocated));

            inventoryService.deductForOutbound(OUTBOUND_ID, MEMBER_ID);

            assertThat(allocated.getQuantity()).isZero();
            verify(inventoryPort).save(allocated);
            verify(inventoryPort).delete(allocated);
        }
    }
}