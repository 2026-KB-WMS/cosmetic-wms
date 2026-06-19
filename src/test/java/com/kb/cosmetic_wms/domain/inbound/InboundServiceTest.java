package com.kb.cosmetic_wms.domain.inbound;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import com.kb.cosmetic_wms.domain.inbound.dto.*;
import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.entity.InboundItem;
import com.kb.cosmetic_wms.domain.inbound.enums.InboundStatus;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundErrorCode;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundItemNotFoundException;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundNotFoundException;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundProductNotFoundException;
import com.kb.cosmetic_wms.domain.inbound.fixture.InboundDtoBuilder;
import com.kb.cosmetic_wms.domain.inbound.fixture.InboundTestBuilder;
import com.kb.cosmetic_wms.domain.inbound.repository.InboundRepository;
import com.kb.cosmetic_wms.domain.inbound.service.InboundService;
import com.kb.cosmetic_wms.domain.partner.entity.Partner;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerErrorCode;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerNotFoundException;
import com.kb.cosmetic_wms.domain.partner.repository.PartnerRepository;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.repository.ProductRepository;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.exception.StorageErrorCode;
import com.kb.cosmetic_wms.domain.storage.exception.WarehouseNotFoundException;
import com.kb.cosmetic_wms.domain.storage.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class InboundServiceTest {

    @InjectMocks
    private InboundService inboundService;

    @Mock
    private InboundRepository inboundRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private PartnerRepository partnerRepository;
    @Mock
    private ProductRepository productRepository;

    private Inbound defaultInbound;

    @BeforeEach
    void setUp() {
        defaultInbound = new InboundTestBuilder().build();
        ReflectionTestUtils.setField(defaultInbound, "id", 1L);
    }

    // =========================================================
    // 입고 전표 등록
    // =========================================================

    @Nested
    class 입고_전표_등록 {

        @Test
        void 창고와_파트너가_존재하면_SCHEDULED_상태로_입고_전표가_등록된다() {
            // given
            InboundCreateRequestDto request = new InboundDtoBuilder().buildCreateRequest();

            given(warehouseRepository.findById(1L)).willReturn(Optional.of(mock(Warehouse.class)));
            given(partnerRepository.findById(1L)).willReturn(Optional.of(mock(Partner.class)));
            given(inboundRepository.save(any(Inbound.class))).willReturn(defaultInbound);

            // when
            InboundDetailResponseDto result = inboundService.registerInbound(request);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.SCHEDULED);
            assertThat(result.warehouseId()).isEqualTo(1L);
            assertThat(result.partnerId()).isEqualTo(1L);
        }

        @Test
        void 존재하지_않는_창고_ID로_등록하면_WarehouseNotFoundException이_발생한다() {
            // given
            InboundCreateRequestDto request = new InboundDtoBuilder().warehouseId(999L).buildCreateRequest();
            given(warehouseRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.registerInbound(request))
                    .isInstanceOf(WarehouseNotFoundException.class)
                    .hasMessage(StorageErrorCode.STORAGE_NOT_FOUND.getMessage());
        }

        @Test
        void 존재하지_않는_파트너_ID로_등록하면_PartnerNotFoundException이_발생한다() {
            // given
            InboundCreateRequestDto request = new InboundDtoBuilder().partnerId(999L).buildCreateRequest();
            given(warehouseRepository.findById(1L)).willReturn(Optional.of(mock(Warehouse.class)));
            given(partnerRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.registerInbound(request))
                    .isInstanceOf(PartnerNotFoundException.class)
                    .hasMessage(PartnerErrorCode.PARTNER_NOT_FOUND.getMessage());
        }
    }

    // =========================================================
    // 입고 품목 추가
    // =========================================================

    @Nested
    class 입고_품목_추가 {

        @Test
        void SCHEDULED_상태의_입고_전표에_품목을_추가하면_WAITING_상태의_품목이_포함된_전표가_반환된다() {
            // given
            InboundItemAddRequestDto request = new InboundDtoBuilder().buildAddItemRequest();
            given(inboundRepository.findById(1L)).willReturn(Optional.of(defaultInbound));
            given(productRepository.findById(1L)).willReturn(Optional.of(mock(Product.class)));

            // when
            InboundDetailResponseDto result = inboundService.addItem(1L, request);

            // then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).productId()).isEqualTo(1L);
            assertThat(result.items().get(0).inspectionStatus()).isEqualTo(InspectionStatus.WAITING);
        }

        @Test
        void 존재하지_않는_입고_ID로_품목을_추가하면_InboundNotFoundException이_발생한다() {
            // given
            given(inboundRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.addItem(999L, new InboundDtoBuilder().buildAddItemRequest()))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }

        @Test
        void 존재하지_않는_상품_ID로_품목을_추가하면_InboundProductNotFoundException이_발생한다() {
            // given
            InboundItemAddRequestDto request = new InboundDtoBuilder().productId(999L).buildAddItemRequest();
            given(inboundRepository.findById(1L)).willReturn(Optional.of(defaultInbound));
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.addItem(1L, request))
                    .isInstanceOf(InboundProductNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_PRODUCT_NOT_FOUND.getMessage());
        }

        @Test
        void SCHEDULED_이외_상태의_입고_전표에_품목_추가를_시도하면_IllegalStateException이_전파된다() {
            // given - IN_PROGRESS 상태의 입고 전표
            Inbound inProgress = new InboundTestBuilder().buildInProgress();
            ReflectionTestUtils.setField(inProgress, "id", 1L);
            given(inboundRepository.findById(1L)).willReturn(Optional.of(inProgress));
            given(productRepository.findById(1L)).willReturn(Optional.of(mock(Product.class)));

            // when & then
            assertThatThrownBy(() -> inboundService.addItem(1L, new InboundDtoBuilder().buildAddItemRequest()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(InboundConstants.INVALID_ADD_ITEM_MESSAGE);
        }
    }

    // =========================================================
    // 입고 작업 시작
    // =========================================================

    @Nested
    class 입고_작업_시작 {

        @Test
        void SCHEDULED_상태의_입고_전표를_시작하면_IN_PROGRESS_상태로_전환된다() {
            // given
            given(inboundRepository.findById(1L)).willReturn(Optional.of(defaultInbound));

            // when
            InboundDetailResponseDto result = inboundService.startInbound(1L);

            // then
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.IN_PROGRESS);
        }

        @Test
        void 존재하지_않는_입고_ID로_작업_시작을_요청하면_InboundNotFoundException이_발생한다() {
            // given
            given(inboundRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.startInbound(999L))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_진행_중인_입고_전표에_작업_시작을_요청하면_IllegalStateException이_전파된다() {
            // given
            Inbound inProgress = new InboundTestBuilder().buildInProgress();
            ReflectionTestUtils.setField(inProgress, "id", 1L);
            given(inboundRepository.findById(1L)).willReturn(Optional.of(inProgress));

            // when & then
            assertThatThrownBy(() -> inboundService.startInbound(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("입고 예정 상태에서만 작업을 시작할 수 있습니다");
        }
    }

    // =========================================================
    // 실물 적재 완료 (Putaway)
    // =========================================================

    @Nested
    class 실물_적재_완료 {

        @Test
        void WAITING_상태의_품목에_로트와_섹션을_지정하면_INSPECTING_상태로_전환되고_ID가_할당된다() {
            // given
            Inbound inbound = new InboundTestBuilder().build();
            InboundItem item = inbound.addItem(new InboundLine(1L, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(2)));
            ReflectionTestUtils.setField(inbound, "id", 1L);
            ReflectionTestUtils.setField(item, "id", 1L);
            given(inboundRepository.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when
            InboundItemResponseDto result = inboundService.completePutaway(1L, 1L, new InboundPutawayRequestDto(100L, 200L));

            // then
            assertThat(result.inspectionStatus()).isEqualTo(InspectionStatus.INSPECTING);
            assertThat(result.lotId()).isEqualTo(100L);
            assertThat(result.sectionId()).isEqualTo(200L);
        }

        @Test
        void 존재하지_않는_품목_ID로_적재_완료를_요청하면_InboundItemNotFoundException이_발생한다() {
            // given - 품목이 없는 Inbound
            Inbound inbound = new InboundTestBuilder().build();
            ReflectionTestUtils.setField(inbound, "id", 1L);
            given(inboundRepository.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.completePutaway(1L, 999L, new InboundPutawayRequestDto(100L, 200L)))
                    .isInstanceOf(InboundItemNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_ITEM_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_적재가_완료된_품목에_다시_적재를_요청하면_IllegalStateException이_전파된다() {
            // given - INSPECTING 상태 품목을 가진 Inbound
            Inbound inbound = new InboundTestBuilder().build();
            InboundItem item = inbound.addItem(new InboundLine(1L, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(2)));
            item.completePutaway(100L, 200L);
            ReflectionTestUtils.setField(inbound, "id", 1L);
            ReflectionTestUtils.setField(item, "id", 1L);
            given(inboundRepository.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.completePutaway(1L, 1L, new InboundPutawayRequestDto(100L, 200L)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 적재가 완료되었거나 검수가 진행된 품목입니다");
        }
    }

    // =========================================================
    // 검수 정상 완료
    // =========================================================

    @Nested
    class 검수_정상_완료 {

        @Test
        void INSPECTING_상태의_품목을_정상_완료하면_NORMAL_상태로_전환된다() {
            // given
            Inbound inbound = new InboundTestBuilder().build();
            InboundItem item = inbound.addItem(new InboundLine(1L, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(2)));
            item.completePutaway(100L, 200L);
            ReflectionTestUtils.setField(inbound, "id", 1L);
            ReflectionTestUtils.setField(item, "id", 1L);
            given(inboundRepository.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when
            InboundItemResponseDto result = inboundService.approveItem(1L, 1L);

            // then
            assertThat(result.inspectionStatus()).isEqualTo(InspectionStatus.NORMAL);
        }

        @Test
        void 존재하지_않는_품목_ID로_검수_정상_처리를_요청하면_InboundItemNotFoundException이_발생한다() {
            // given - 품목이 없는 Inbound
            Inbound inbound = new InboundTestBuilder().build();
            ReflectionTestUtils.setField(inbound, "id", 1L);
            given(inboundRepository.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.approveItem(1L, 999L))
                    .isInstanceOf(InboundItemNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_ITEM_NOT_FOUND.getMessage());
        }

        @Test
        void 적재_전_WAITING_상태의_품목에_정상_완료를_요청하면_IllegalStateException이_전파된다() {
            // given - WAITING 상태 품목을 가진 Inbound
            Inbound inbound = new InboundTestBuilder().build();
            InboundItem item = inbound.addItem(new InboundLine(1L, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(2)));
            ReflectionTestUtils.setField(inbound, "id", 1L);
            ReflectionTestUtils.setField(item, "id", 1L);
            given(inboundRepository.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.approveItem(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(InboundConstants.INVALID_NORMAL_STATUS_MESSAGE);
        }
    }

    // =========================================================
    // 검수 보류
    // =========================================================

    @Nested
    class 검수_보류 {

        @Test
        void INSPECTING_상태의_품목을_보류_처리하면_HOLD_상태로_전환된다() {
            // given
            Inbound inbound = new InboundTestBuilder().build();
            InboundItem item = inbound.addItem(new InboundLine(1L, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(2)));
            item.completePutaway(100L, 200L);
            ReflectionTestUtils.setField(inbound, "id", 1L);
            ReflectionTestUtils.setField(item, "id", 1L);
            given(inboundRepository.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when
            InboundItemResponseDto result = inboundService.holdItem(1L, 1L);

            // then
            assertThat(result.inspectionStatus()).isEqualTo(InspectionStatus.HOLD);
        }

        @Test
        void 존재하지_않는_품목_ID로_검수_보류를_요청하면_InboundItemNotFoundException이_발생한다() {
            // given - 품목이 없는 Inbound
            Inbound inbound = new InboundTestBuilder().build();
            ReflectionTestUtils.setField(inbound, "id", 1L);
            given(inboundRepository.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.holdItem(1L, 999L))
                    .isInstanceOf(InboundItemNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_ITEM_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_NORMAL_상태인_품목에_보류를_요청하면_IllegalStateException을_던진다() {
            // given - NORMAL 상태 품목을 가진 Inbound
            Inbound inbound = new InboundTestBuilder().build();
            InboundItem item = inbound.addItem(new InboundLine(1L, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(2)));
            item.completePutaway(100L, 200L);
            item.changeToNormal();
            ReflectionTestUtils.setField(inbound, "id", 1L);
            ReflectionTestUtils.setField(item, "id", 1L);
            given(inboundRepository.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.holdItem(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(InboundConstants.INVALID_HOLD_STATUS_MESSAGE);
        }
    }

    // =========================================================
    // 입고 완료
    // =========================================================

    @Nested
    class 입고_완료 {

        @Test
        void 모든_품목이_NORMAL_상태이면_입고가_COMPLETED_상태로_전환된다() {
            // given - 모든 품목이 NORMAL인 IN_PROGRESS 입고
            Inbound inbound = buildInboundWithInspectedItem(true);
            ReflectionTestUtils.setField(inbound, "id", 1L);
            given(inboundRepository.findById(1L)).willReturn(Optional.of(inbound));

            // when
            InboundDetailResponseDto result = inboundService.completeInbound(1L);

            // then
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.COMPLETED);
        }

        @Test
        void 품목이_HOLD_상태여도_검수가_완료되었으면_입고_완료_처리된다() {
            // given - 품목이 HOLD인 IN_PROGRESS 입고
            Inbound inbound = buildInboundWithHoldItem();
            ReflectionTestUtils.setField(inbound, "id", 1L);
            given(inboundRepository.findById(1L)).willReturn(Optional.of(inbound));

            // when
            InboundDetailResponseDto result = inboundService.completeInbound(1L);

            // then
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.COMPLETED);
        }

        @Test
        void 검수_미완료_품목이_남아있으면_입고_완료_처리_시_IllegalStateException이_전파된다() {
            // given - WAITING 상태 품목이 포함된 IN_PROGRESS 입고
            Inbound inbound = new InboundTestBuilder().build();
            InboundLine line = new InboundLine(1L, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(1));
            inbound.addItem(line); // WAITING 상태
            inbound.startExecution();
            ReflectionTestUtils.setField(inbound, "id", 1L);
            given(inboundRepository.findById(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.completeInbound(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(InboundConstants.INCOMPLETE_INSPECTION_MESSAGE);
        }

        @Test
        void 존재하지_않는_입고_ID로_완료를_요청하면_InboundNotFoundException이_발생한다() {
            // given
            given(inboundRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.completeInbound(999L))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }

        @Test
        void SCHEDULED_상태의_입고에_완료를_요청하면_IllegalStateException이_전파된다() {
            // given - SCHEDULED(기본) 상태 그대로 사용
            given(inboundRepository.findById(1L)).willReturn(Optional.of(defaultInbound));

            // when & then
            assertThatThrownBy(() -> inboundService.completeInbound(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("작업이 진행 중인 상태에서만 입고 완료 처리가 가능합니다");
        }

        private Inbound buildInboundWithInspectedItem(boolean isNormal) {
            Inbound inbound = new InboundTestBuilder().build();
            InboundLine line = new InboundLine(1L, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(1));
            InboundItem item = inbound.addItem(line);
            inbound.startExecution();
            item.completePutaway(100L, 200L);
            if (isNormal) {
                item.changeToNormal();
            } else {
                item.changeToHold();
            }
            return inbound;
        }

        private Inbound buildInboundWithHoldItem() {
            return buildInboundWithInspectedItem(false);
        }
    }

    // =========================================================
    // 입고 취소
    // =========================================================

    @Nested
    class 입고_취소 {

        @Test
        void SCHEDULED_상태의_입고_전표를_취소하면_CANCELED_상태로_전환된다() {
            // given
            given(inboundRepository.findById(1L)).willReturn(Optional.of(defaultInbound));

            // when
            InboundDetailResponseDto result = inboundService.cancelInbound(1L);

            // then
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.CANCELED);
        }

        @Test
        void 존재하지_않는_입고_ID로_취소를_요청하면_InboundNotFoundException이_발생한다() {
            // given
            given(inboundRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.cancelInbound(999L))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_진행_중인_입고에_취소를_요청하면_IllegalStateException이_전파된다() {
            // given
            Inbound inProgress = new InboundTestBuilder().buildInProgress();
            ReflectionTestUtils.setField(inProgress, "id", 1L);
            given(inboundRepository.findById(1L)).willReturn(Optional.of(inProgress));

            // when & then
            assertThatThrownBy(() -> inboundService.cancelInbound(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 작업이 진행되었거나 완료된 입고 건은 취소할 수 없습니다");
        }
    }

    // =========================================================
    // 입고 단건 조회
    // =========================================================

    @Nested
    class 입고_단건_조회 {

        @Test
        void 존재하는_ID로_조회하면_입고_전표_상세정보를_반환한다() {
            // given
            given(inboundRepository.findById(1L)).willReturn(Optional.of(defaultInbound));

            // when
            InboundDetailResponseDto result = inboundService.getInbound(1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.SCHEDULED);
            assertThat(result.warehouseId()).isEqualTo(1L);
            assertThat(result.partnerId()).isEqualTo(1L);
            assertThat(result.items()).isEmpty();
        }

        @Test
        void 존재하지_않는_ID로_조회하면_InboundNotFoundException이_발생한다() {
            // given
            given(inboundRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.getInbound(999L))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }
    }
}
