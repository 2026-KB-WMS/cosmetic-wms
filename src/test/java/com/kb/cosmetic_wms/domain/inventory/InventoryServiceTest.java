package com.kb.cosmetic_wms.domain.inventory;

import com.kb.cosmetic_wms.domain.inventory.constants.InventoryConstants;
import com.kb.cosmetic_wms.domain.inventory.dto.InventoryDetailResponseDto;
import com.kb.cosmetic_wms.domain.inventory.dto.InventoryStatusChangeRequestDto;
import com.kb.cosmetic_wms.domain.inventory.entity.Inventory;
import com.kb.cosmetic_wms.domain.inventory.entity.InventoryTransaction;
import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.TransactionType;
import com.kb.cosmetic_wms.domain.inventory.exception.InventoryErrorCode;
import com.kb.cosmetic_wms.domain.inventory.exception.InventoryNotFoundException;
import com.kb.cosmetic_wms.domain.inventory.fixture.InventoryDtoBuilder;
import com.kb.cosmetic_wms.domain.inventory.fixture.InventoryTestBuilder;
import com.kb.cosmetic_wms.domain.inventory.repository.InventoryRepository;
import com.kb.cosmetic_wms.domain.inventory.repository.InventoryTransactionRepository;
import com.kb.cosmetic_wms.domain.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    private Inventory defaultInventory;

    @BeforeEach
    void setUp() {
        // quantity=100, availableQuantity=100, UNALLOCATED/NORMAL/STORED
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
            // given
            given(inventoryRepository.findById(1L)).willReturn(Optional.of(defaultInventory));

            // when
            InventoryDetailResponseDto result = inventoryService.getInventory(1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.quantity()).isEqualTo(100);
            assertThat(result.availableQuantity()).isEqualTo(100);
            assertThat(result.allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);
            assertThat(result.qualityStatus()).isEqualTo(QualityStatus.NORMAL);
            assertThat(result.locStatus()).isEqualTo(LocStatus.STORED);
        }

        @Test
        void 존재하지_않는_ID로_조회하면_InventoryNotFoundException이_발생한다() {
            // given
            given(inventoryRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inventoryService.getInventory(999L))
                    .isInstanceOf(InventoryNotFoundException.class)
                    .hasMessage(InventoryErrorCode.INVENTORY_NOT_FOUND.getMessage());
        }

        @Test
        void LOT_ID로_조회하면_해당_LOT의_전체_재고_목록을_반환한다() {
            // given
            Inventory secondInventory = new InventoryTestBuilder().quantity(50).availableQuantity(50).build();
            ReflectionTestUtils.setField(secondInventory, "id", 2L);

            given(inventoryRepository.findByLotId(10L)).willReturn(List.of(defaultInventory, secondInventory));

            // when
            List<InventoryDetailResponseDto> result = inventoryService.getInventoriesByLotId(10L);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(1).id()).isEqualTo(2L);
            assertThat(result.get(1).quantity()).isEqualTo(50);
        }

        @Test
        void 해당_LOT에_재고가_없으면_빈_목록을_반환한다() {
            // given
            given(inventoryRepository.findByLotId(10L)).willReturn(List.of());

            // when
            List<InventoryDetailResponseDto> result = inventoryService.getInventoriesByLotId(10L);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 상품_ID로_조회하면_해당_상품의_전체_재고_목록을_반환한다() {
            // given
            given(inventoryRepository.findByProductId(1L)).willReturn(List.of(defaultInventory));

            // when
            List<InventoryDetailResponseDto> result = inventoryService.getInventoriesByProductId(1L);

            // then
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
            // given - quantity=100 전체 할당
            InventoryStatusChangeRequestDto request = new InventoryDtoBuilder()
                    .quantity(100).referenceId(10L).memberId(1L).build();

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(defaultInventory));

            // when
            InventoryDetailResponseDto result = inventoryService.allocate(1L, request);

            // then
            assertThat(result.allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
            assertThat(result.availableQuantity()).isEqualTo(0);
            // 전체 할당 → split 없음 → save() 호출 안 됨
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        void 부분_수량을_할당하면_원본_수량이_감소하고_ALLOCATED_분할_재고가_저장된다() {
            // given - 100 중 30만 할당
            InventoryStatusChangeRequestDto request = new InventoryDtoBuilder()
                    .quantity(30).referenceId(10L).memberId(1L).build();

            Inventory splitResult = new InventoryTestBuilder()
                    .quantity(30).availableQuantity(0)
                    .allocStatus(AllocStatus.ALLOCATED).build();
            ReflectionTestUtils.setField(splitResult, "id", 2L);

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(defaultInventory));
            given(inventoryRepository.save(any(Inventory.class))).willReturn(splitResult);

            // when
            InventoryDetailResponseDto result = inventoryService.allocate(1L, request);

            // then - 분할된 재고(ALLOCATED) 기준 응답
            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.quantity()).isEqualTo(30);
            assertThat(result.allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
            // 원본 수량도 감소
            assertThat(defaultInventory.getQuantity()).isEqualTo(70);
            // split 재고가 save() 됨
            verify(inventoryRepository, times(1)).save(any(Inventory.class));
        }

        @Test
        void 할당_성공_시_ALLOCATE_트랜잭션_이력이_생성된다() {
            // given - 전체 할당
            InventoryStatusChangeRequestDto request = new InventoryDtoBuilder()
                    .quantity(100).referenceId(10L).memberId(1L).build();

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(defaultInventory));

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            // when
            inventoryService.allocate(1L, request);

            // then
            verify(inventoryTransactionRepository).save(txCaptor.capture());
            InventoryTransaction recorded = txCaptor.getValue();
            assertThat(recorded.getTransactionType()).isEqualTo(TransactionType.ALLOCATE);
            assertThat(recorded.getTransactionQuantity()).isEqualTo(100);
            assertThat(recorded.getCurrStatusSet().allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
        }

        @Test
        void 재고가_없으면_InventoryNotFoundException이_발생한다() {
            // given
            given(inventoryRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inventoryService.allocate(999L,
                    new InventoryDtoBuilder().quantity(10).build()))
                    .isInstanceOf(InventoryNotFoundException.class);
        }

        @Test
        void 가용_수량을_초과하는_할당_요청은_IllegalArgumentException이_전파된다() {
            // given - 가용 수량(100)을 초과하는 요청
            given(inventoryRepository.findById(1L)).willReturn(Optional.of(defaultInventory));

            // when & then
            assertThatThrownBy(() -> inventoryService.allocate(1L,
                    new InventoryDtoBuilder().quantity(200).referenceId(10L).build()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(InventoryConstants.LACK_OF_AVAILABLE_QUANTITY_MESSAGE);
        }
    }

    // =========================================================
    // 할당 취소 (unallocate)
    // =========================================================

    @Nested
    class 할당_취소 {

        @Test
        void ALLOCATED_재고를_전체_취소하면_UNALLOCATED_상태로_복귀되고_이력이_기록된다() {
            // given - ALLOCATED 재고 준비
            Inventory allocated = new InventoryTestBuilder()
                    .quantity(50).availableQuantity(0)
                    .allocStatus(AllocStatus.ALLOCATED).build();
            ReflectionTestUtils.setField(allocated, "id", 1L);

            InventoryStatusChangeRequestDto request = new InventoryDtoBuilder()
                    .quantity(50).referenceId(10L).memberId(1L).build();

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(allocated));

            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            // when
            InventoryDetailResponseDto result = inventoryService.unallocate(1L, request);

            // then
            assertThat(result.allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);
            verify(inventoryTransactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.UNALLOCATE);
        }

        @Test
        void UNALLOCATED_재고에_할당_취소를_요청하면_IllegalStateException이_전파된다() {
            // given - 이미 UNALLOCATED 상태
            given(inventoryRepository.findById(1L)).willReturn(Optional.of(defaultInventory));

            // when & then
            assertThatThrownBy(() -> inventoryService.unallocate(1L,
                    new InventoryDtoBuilder().quantity(50).build()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(InventoryConstants.UNALLOCATE_FOR_ALLOCATED_ONLY_MESSAGE);
        }

        @Test
        void 재고가_없으면_InventoryNotFoundException이_발생한다() {
            // given
            given(inventoryRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
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
            // given
            InventoryStatusChangeRequestDto request = new InventoryDtoBuilder()
                    .quantity(100).buildWithoutRef();

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(defaultInventory));
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            // when
            InventoryDetailResponseDto result = inventoryService.startMoving(1L, request);

            // then
            assertThat(result.locStatus()).isEqualTo(LocStatus.MOVING);
            verify(inventoryTransactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.LOCATION_MOVE);
        }

        @Test
        void MOVING_재고를_이동_완료하면_STORED_상태로_복귀하고_이력이_기록된다() {
            // given - MOVING 재고 준비
            Inventory movingInventory = new InventoryTestBuilder()
                    .locStatus(LocStatus.MOVING).build();
            ReflectionTestUtils.setField(movingInventory, "id", 1L);

            InventoryStatusChangeRequestDto request = new InventoryDtoBuilder()
                    .quantity(100).buildWithoutRef();

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(movingInventory));
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            // when
            InventoryDetailResponseDto result = inventoryService.finishMoving(1L, request);

            // then
            assertThat(result.locStatus()).isEqualTo(LocStatus.STORED);
            verify(inventoryTransactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.LOCATION_MOVE);
        }

        @Test
        void ALLOCATED_재고를_이동_시작하면_IllegalStateException이_전파된다() {
            // given
            Inventory allocated = new InventoryTestBuilder()
                    .allocStatus(AllocStatus.ALLOCATED).availableQuantity(0).build();
            ReflectionTestUtils.setField(allocated, "id", 1L);

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(allocated));

            // when & then
            assertThatThrownBy(() -> inventoryService.startMoving(1L,
                    new InventoryDtoBuilder().quantity(100).buildWithoutRef()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(InventoryConstants.START_MOVING_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        @Test
        void 재고가_없으면_startMoving에서_InventoryNotFoundException이_발생한다() {
            // given
            given(inventoryRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
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
            // given
            InventoryStatusChangeRequestDto request = new InventoryDtoBuilder()
                    .quantity(100).buildWithoutRef();

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(defaultInventory));
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            // when
            InventoryDetailResponseDto result = inventoryService.startInspecting(1L, request);

            // then
            assertThat(result.qualityStatus()).isEqualTo(QualityStatus.INSPECTING);
            assertThat(result.availableQuantity()).isEqualTo(0);
            verify(inventoryTransactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.QUALITY_INSPECTING);
        }

        @Test
        void INSPECTING_재고를_정상으로_복귀하면_NORMAL_상태가_되고_이력이_기록된다() {
            // given
            Inventory inspecting = new InventoryTestBuilder()
                    .qualityStatus(QualityStatus.INSPECTING).availableQuantity(0).build();
            ReflectionTestUtils.setField(inspecting, "id", 1L);

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(inspecting));
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            // when
            InventoryDetailResponseDto result = inventoryService.restoreToNormalQuality(1L,
                    new InventoryDtoBuilder().quantity(100).buildWithoutRef());

            // then
            assertThat(result.qualityStatus()).isEqualTo(QualityStatus.NORMAL);
            verify(inventoryTransactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.QUALITY_RELEASE);
        }

        @Test
        void 정상_재고를_HOLD_상태로_변경하면_출고_가능_수량이_0이_되고_이력이_기록된다() {
            // given
            InventoryStatusChangeRequestDto request = new InventoryDtoBuilder()
                    .quantity(100).buildWithoutRef();

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(defaultInventory));
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            // when
            InventoryDetailResponseDto result = inventoryService.holdForQualityIssue(1L, request);

            // then
            assertThat(result.qualityStatus()).isEqualTo(QualityStatus.HOLD);
            assertThat(result.availableQuantity()).isEqualTo(0);
            verify(inventoryTransactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.QUALITY_HOLD);
        }

        @Test
        void 정상_재고를_폐기_예정으로_변경하면_DISCARD_SCHEDULED_상태가_되고_이력이_기록된다() {
            // given
            given(inventoryRepository.findById(1L)).willReturn(Optional.of(defaultInventory));
            ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

            // when
            InventoryDetailResponseDto result = inventoryService.scheduleForDiscard(1L,
                    new InventoryDtoBuilder().quantity(100).buildWithoutRef());

            // then
            assertThat(result.qualityStatus()).isEqualTo(QualityStatus.DISCARD_SCHEDULED);
            verify(inventoryTransactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.DISCARD);
        }

        @Test
        void 할당된_재고에_품질_HOLD를_요청하면_IllegalStateException이_전파된다() {
            // given
            Inventory allocated = new InventoryTestBuilder()
                    .allocStatus(AllocStatus.ALLOCATED).availableQuantity(0).build();
            ReflectionTestUtils.setField(allocated, "id", 1L);

            given(inventoryRepository.findById(1L)).willReturn(Optional.of(allocated));

            // when & then
            assertThatThrownBy(() -> inventoryService.holdForQualityIssue(1L,
                    new InventoryDtoBuilder().quantity(100).buildWithoutRef()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(InventoryConstants.HOLD_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        @Test
        void 재고가_없으면_품질_상태_변경에서_InventoryNotFoundException이_발생한다() {
            // given
            given(inventoryRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inventoryService.startInspecting(999L,
                    new InventoryDtoBuilder().quantity(10).buildWithoutRef()))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }
}
