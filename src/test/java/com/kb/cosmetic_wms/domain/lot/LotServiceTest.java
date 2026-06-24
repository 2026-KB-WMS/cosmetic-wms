package com.kb.cosmetic_wms.domain.lot;

import com.kb.cosmetic_wms.domain.lot.dto.LotCreateRequestDto;
import com.kb.cosmetic_wms.domain.lot.dto.LotDetailResponseDto;
import com.kb.cosmetic_wms.domain.lot.dto.LotStatusUpdateRequestDto;
import com.kb.cosmetic_wms.domain.lot.entity.Lot;
import com.kb.cosmetic_wms.domain.lot.enums.LotStatus;
import com.kb.cosmetic_wms.domain.lot.exception.DuplicateLotNumberException;
import com.kb.cosmetic_wms.domain.lot.exception.LotErrorCode;
import com.kb.cosmetic_wms.domain.lot.exception.LotNotFoundException;
import com.kb.cosmetic_wms.domain.lot.exception.LotProductNotFoundException;
import com.kb.cosmetic_wms.domain.lot.fixture.LotDtoBuilder;
import com.kb.cosmetic_wms.domain.lot.fixture.LotTestBuilder;
import com.kb.cosmetic_wms.domain.lot.repository.LotRepository;
import com.kb.cosmetic_wms.domain.lot.service.LotService;
import com.kb.cosmetic_wms.product.application.port.in.FindProductUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LotServiceTest {

    @InjectMocks
    private LotService lotService;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private FindProductUseCase findProductUseCase;

    private Lot defaultLot;

    @BeforeEach
    void setUp() {
        defaultLot = new LotTestBuilder().build();
        ReflectionTestUtils.setField(defaultLot, "id", 1L);
    }

    @Nested
    class 로트_등록 {

        @Test
        void 올바른_정보로_등록하면_로트가_저장되고_초기_상태는_AVAILABLE이다() {
            // given
            LotCreateRequestDto request = new LotDtoBuilder().build();

            given(findProductUseCase.existsById(request.productId())).willReturn(true);
            given(lotRepository.existsByLotNumber(request.lotNumber())).willReturn(false);
            given(lotRepository.save(any(Lot.class))).willReturn(defaultLot);

            // when
            LotDetailResponseDto result = lotService.register(request);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.lotNumber()).isEqualTo("SKN-240101-01-0001");
            assertThat(result.status()).isEqualTo(LotStatus.AVAILABLE);
            assertThat(result.productId()).isEqualTo(1L);
        }

        @Test
        void 등록된_로트의_제조일자와_유통기한이_응답에_포함된다() {
            // given
            LotCreateRequestDto request = new LotDtoBuilder().build();

            given(findProductUseCase.existsById(request.productId())).willReturn(true);
            given(lotRepository.existsByLotNumber(request.lotNumber())).willReturn(false);
            given(lotRepository.save(any(Lot.class))).willReturn(defaultLot);

            // when
            LotDetailResponseDto result = lotService.register(request);

            // then
            assertThat(result.manufacturingDate()).isEqualTo(defaultLot.getManufacturingDate());
            assertThat(result.expirationDate()).isEqualTo(defaultLot.getExpirationDate());
        }

        @Test
        void 존재하지_않는_상품_ID로_등록하면_LotProductNotFoundException이_발생한다() {
            // given
            LotCreateRequestDto request = new LotDtoBuilder().productId(999L).build();

            given(findProductUseCase.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> lotService.register(request))
                    .isInstanceOf(LotProductNotFoundException.class)
                    .hasMessage(LotErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_등록된_로트_번호로_등록하면_DuplicateLotNumberException이_발생한다() {
            // given
            LotCreateRequestDto request = new LotDtoBuilder().build();

            given(findProductUseCase.existsById(request.productId())).willReturn(true);
            given(lotRepository.existsByLotNumber(request.lotNumber())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> lotService.register(request))
                    .isInstanceOf(DuplicateLotNumberException.class)
                    .hasMessage(LotErrorCode.DUPLICATE_LOT_NUMBER.getMessage());
        }
    }

    @Nested
    class 로트_단건_조회 {

        @Test
        void 존재하는_ID로_조회하면_로트_상세_정보를_반환한다() {
            // given
            given(lotRepository.findById(1L)).willReturn(Optional.of(defaultLot));

            // when
            LotDetailResponseDto result = lotService.getLot(1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.lotNumber()).isEqualTo("SKN-240101-01-0001");
            assertThat(result.status()).isEqualTo(LotStatus.AVAILABLE);
            assertThat(result.productId()).isEqualTo(1L);
        }

        @Test
        void 존재하지_않는_ID로_조회하면_LotNotFoundException이_발생한다() {
            // given
            given(lotRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> lotService.getLot(999L))
                    .isInstanceOf(LotNotFoundException.class)
                    .hasMessage(LotErrorCode.LOT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 상품별_로트_목록_조회 {

        @Test
        void 상품에_등록된_로트가_여러_개이면_전체_목록을_반환한다() {
            // given
            Lot secondLot = new LotTestBuilder().lotNumber("SKN-240101-01-0002").build();
            ReflectionTestUtils.setField(secondLot, "id", 2L);

            given(findProductUseCase.existsById(1L)).willReturn(true);
            given(lotRepository.findByProductId(1L)).willReturn(List.of(defaultLot, secondLot));

            // when
            List<LotDetailResponseDto> result = lotService.getLotsByProductId(1L);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(1).id()).isEqualTo(2L);
            assertThat(result.get(1).lotNumber()).isEqualTo("SKN-240101-01-0002");
        }

        @Test
        void 등록된_로트가_없으면_빈_목록을_반환한다() {
            // given
            given(findProductUseCase.existsById(1L)).willReturn(true);
            given(lotRepository.findByProductId(1L)).willReturn(List.of());

            // when
            List<LotDetailResponseDto> result = lotService.getLotsByProductId(1L);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 존재하지_않는_상품_ID로_조회하면_LotProductNotFoundException이_발생한다() {
            // given
            given(findProductUseCase.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> lotService.getLotsByProductId(999L))
                    .isInstanceOf(LotProductNotFoundException.class)
                    .hasMessage(LotErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 로트_상태_변경 {

        @Test
        void AVAILABLE_상태의_로트를_HOLD로_변경하면_상태가_갱신되어_반환된다() {
            // given
            given(lotRepository.findById(1L)).willReturn(Optional.of(defaultLot));

            // when
            LotDetailResponseDto result = lotService.updateLotStatus(1L, new LotStatusUpdateRequestDto(LotStatus.HOLD));

            // then
            assertThat(result.status()).isEqualTo(LotStatus.HOLD);
        }

        @Test
        void 로트_상태를_RECALLED로_변경하면_상태가_갱신되어_반환된다() {
            // given
            given(lotRepository.findById(1L)).willReturn(Optional.of(defaultLot));

            // when
            LotDetailResponseDto result = lotService.updateLotStatus(1L, new LotStatusUpdateRequestDto(LotStatus.RECALLED));

            // then
            assertThat(result.status()).isEqualTo(LotStatus.RECALLED);
        }

        @Test
        void 로트_상태를_DISPOSED로_변경하면_폐기_상태가_반환된다() {
            // given
            given(lotRepository.findById(1L)).willReturn(Optional.of(defaultLot));

            // when
            LotDetailResponseDto result = lotService.updateLotStatus(1L, new LotStatusUpdateRequestDto(LotStatus.DISPOSED));

            // then
            assertThat(result.status()).isEqualTo(LotStatus.DISPOSED);
        }

        @Test
        void 존재하지_않는_ID로_상태_변경을_요청하면_LotNotFoundException이_발생한다() {
            // given
            given(lotRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> lotService.updateLotStatus(999L, new LotStatusUpdateRequestDto(LotStatus.HOLD)))
                    .isInstanceOf(LotNotFoundException.class)
                    .hasMessage(LotErrorCode.LOT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 로트_삭제 {

        @Test
        void 존재하는_로트를_삭제하면_레포지토리_delete가_호출된다() {
            // given
            given(lotRepository.findById(1L)).willReturn(Optional.of(defaultLot));

            // when
            lotService.deleteLot(1L);

            // then
            verify(lotRepository).delete(defaultLot);
        }

        @Test
        void 존재하지_않는_ID로_삭제를_요청하면_LotNotFoundException이_발생한다() {
            // given
            given(lotRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> lotService.deleteLot(999L))
                    .isInstanceOf(LotNotFoundException.class)
                    .hasMessage(LotErrorCode.LOT_NOT_FOUND.getMessage());
        }
    }
}
