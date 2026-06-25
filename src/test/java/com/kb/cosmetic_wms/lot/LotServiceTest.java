package com.kb.cosmetic_wms.lot;

import com.kb.cosmetic_wms.lot.application.port.in.*;
import com.kb.cosmetic_wms.lot.application.port.out.LotPort;
import com.kb.cosmetic_wms.lot.application.service.LotService;
import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import com.kb.cosmetic_wms.lot.domain.exception.DuplicateLotNumberException;
import com.kb.cosmetic_wms.lot.domain.exception.LotErrorCode;
import com.kb.cosmetic_wms.lot.domain.exception.LotNotFoundException;
import com.kb.cosmetic_wms.lot.domain.exception.LotProductNotFoundException;
import com.kb.cosmetic_wms.lot.domain.model.Lot;
import com.kb.cosmetic_wms.lot.fixture.LotCommandBuilder;
import com.kb.cosmetic_wms.lot.fixture.LotTestBuilder;
import com.kb.cosmetic_wms.product.product.application.port.in.FindProductUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private LotPort lotPort;

    @Mock
    private FindProductUseCase findProductUseCase;

    private Lot defaultLot;

    @BeforeEach
    void setUp() {
        defaultLot = new LotTestBuilder().buildWithId(1L);
    }

    @Nested
    class 로트_등록 {

        @Test
        void 올바른_정보로_등록하면_로트가_저장되고_초기_상태는_AVAILABLE이다() {
            RegisterLotCommand command = new LotCommandBuilder().build();

            given(findProductUseCase.existsById(command.productId())).willReturn(true);
            given(lotPort.existsByLotNumber(command.lotNumber())).willReturn(false);
            given(lotPort.save(any(Lot.class))).willReturn(defaultLot);

            LotResult result = lotService.register(command);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.lotNumber()).isEqualTo("SKN-240101-01-0001");
            assertThat(result.status()).isEqualTo(LotStatus.AVAILABLE);
            assertThat(result.productId()).isEqualTo(1L);
        }

        @Test
        void 등록된_로트의_제조일자와_유통기한이_응답에_포함된다() {
            RegisterLotCommand command = new LotCommandBuilder().build();

            given(findProductUseCase.existsById(command.productId())).willReturn(true);
            given(lotPort.existsByLotNumber(command.lotNumber())).willReturn(false);
            given(lotPort.save(any(Lot.class))).willReturn(defaultLot);

            LotResult result = lotService.register(command);

            assertThat(result.manufacturingDate()).isEqualTo(defaultLot.getManufacturingDate());
            assertThat(result.expirationDate()).isEqualTo(defaultLot.getExpirationDate());
        }

        @Test
        void 존재하지_않는_상품_ID로_등록하면_LotProductNotFoundException이_발생한다() {
            RegisterLotCommand command = new LotCommandBuilder().productId(999L).build();

            given(findProductUseCase.existsById(999L)).willReturn(false);

            assertThatThrownBy(() -> lotService.register(command))
                    .isInstanceOf(LotProductNotFoundException.class)
                    .hasMessage(LotErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_등록된_로트_번호로_등록하면_DuplicateLotNumberException이_발생한다() {
            RegisterLotCommand command = new LotCommandBuilder().build();

            given(findProductUseCase.existsById(command.productId())).willReturn(true);
            given(lotPort.existsByLotNumber(command.lotNumber())).willReturn(true);

            assertThatThrownBy(() -> lotService.register(command))
                    .isInstanceOf(DuplicateLotNumberException.class)
                    .hasMessage(LotErrorCode.DUPLICATE_LOT_NUMBER.getMessage());
        }
    }

    @Nested
    class 로트_단건_조회 {

        @Test
        void 존재하는_ID로_조회하면_로트_상세_정보를_반환한다() {
            given(lotPort.findById(1L)).willReturn(Optional.of(defaultLot));

            LotResult result = lotService.findById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.lotNumber()).isEqualTo("SKN-240101-01-0001");
            assertThat(result.status()).isEqualTo(LotStatus.AVAILABLE);
            assertThat(result.productId()).isEqualTo(1L);
        }

        @Test
        void 존재하지_않는_ID로_조회하면_LotNotFoundException이_발생한다() {
            given(lotPort.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> lotService.findById(999L))
                    .isInstanceOf(LotNotFoundException.class)
                    .hasMessage(LotErrorCode.LOT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 상품별_로트_목록_조회 {

        @Test
        void 상품에_등록된_로트가_여러_개이면_전체_목록을_반환한다() {
            Lot secondLot = new LotTestBuilder().lotNumber("SKN-240101-01-0002").buildWithId(2L);

            given(findProductUseCase.existsById(1L)).willReturn(true);
            given(lotPort.findByProductId(1L)).willReturn(List.of(defaultLot, secondLot));

            List<LotResult> result = lotService.findByProductId(1L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(1).id()).isEqualTo(2L);
            assertThat(result.get(1).lotNumber()).isEqualTo("SKN-240101-01-0002");
        }

        @Test
        void 등록된_로트가_없으면_빈_목록을_반환한다() {
            given(findProductUseCase.existsById(1L)).willReturn(true);
            given(lotPort.findByProductId(1L)).willReturn(List.of());

            List<LotResult> result = lotService.findByProductId(1L);

            assertThat(result).isEmpty();
        }

        @Test
        void 존재하지_않는_상품_ID로_조회하면_LotProductNotFoundException이_발생한다() {
            given(findProductUseCase.existsById(999L)).willReturn(false);

            assertThatThrownBy(() -> lotService.findByProductId(999L))
                    .isInstanceOf(LotProductNotFoundException.class)
                    .hasMessage(LotErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 로트_상태_변경 {

        @Test
        void AVAILABLE_상태의_로트를_HOLD로_변경하면_상태가_갱신되어_반환된다() {
            given(lotPort.findById(1L)).willReturn(Optional.of(defaultLot));
            given(lotPort.save(any(Lot.class))).willAnswer(inv -> inv.getArgument(0));

            LotResult result = lotService.updateStatus(1L, new UpdateLotStatusCommand(LotStatus.HOLD));

            assertThat(result.status()).isEqualTo(LotStatus.HOLD);
        }

        @Test
        void 로트_상태를_RECALLED로_변경하면_상태가_갱신되어_반환된다() {
            given(lotPort.findById(1L)).willReturn(Optional.of(defaultLot));
            given(lotPort.save(any(Lot.class))).willAnswer(inv -> inv.getArgument(0));

            LotResult result = lotService.updateStatus(1L, new UpdateLotStatusCommand(LotStatus.RECALLED));

            assertThat(result.status()).isEqualTo(LotStatus.RECALLED);
        }

        @Test
        void 로트_상태를_DISPOSED로_변경하면_폐기_상태가_반환된다() {
            given(lotPort.findById(1L)).willReturn(Optional.of(defaultLot));
            given(lotPort.save(any(Lot.class))).willAnswer(inv -> inv.getArgument(0));

            LotResult result = lotService.updateStatus(1L, new UpdateLotStatusCommand(LotStatus.DISPOSED));

            assertThat(result.status()).isEqualTo(LotStatus.DISPOSED);
        }

        @Test
        void 존재하지_않는_ID로_상태_변경을_요청하면_LotNotFoundException이_발생한다() {
            given(lotPort.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> lotService.updateStatus(999L, new UpdateLotStatusCommand(LotStatus.HOLD)))
                    .isInstanceOf(LotNotFoundException.class)
                    .hasMessage(LotErrorCode.LOT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 로트_삭제 {

        @Test
        void 존재하는_로트를_삭제하면_포트_delete가_호출된다() {
            given(lotPort.findById(1L)).willReturn(Optional.of(defaultLot));

            lotService.delete(1L);

            verify(lotPort).delete(defaultLot);
        }

        @Test
        void 존재하지_않는_ID로_삭제를_요청하면_LotNotFoundException이_발생한다() {
            given(lotPort.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> lotService.delete(999L))
                    .isInstanceOf(LotNotFoundException.class)
                    .hasMessage(LotErrorCode.LOT_NOT_FOUND.getMessage());
        }
    }
}