package com.kb.cosmetic_wms.domain.order;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

//    @InjectMocks
//    private OrderService orderService;
//
//    @Mock
//    private OrderRepository orderRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Nested
    class 발주_등록_및_생성 {

        @Test
        void 올바른_발주_요청_정보가_주어지면_발주_전표가_성공적으로_생성된다() {
        }

        @Test
        void 발주_요청_시_납품_예정일이_과거_날짜이거나_누락되면_예외가_발생한다() {
        }
    }

    @Nested
    class 발주_품목_추가_및_변경 {

        @Test
        void 임시저장_상태인_발주_전표에는_새로운_발주_대상_상품과_수량을_정상적으로_추가한다() {
        }

        @Test
        void 이미_확정되었거나_취소된_발주_전표에_품목을_추가하려고_하면_예외가_발생한다() {
        }

        @Test
        void 추가하려는_발주_품목의_수량이_0_이하인_경우_예외가_발생한다() {
        }

        @Test
        void 기존에_등록된_발주_품목의_수량을_변경하면_전표의_총_발주_수량과_금액이_재계산된다() {
        }
    }

    @Nested
    class 발주_확정 {

        @Test
        void 임시저장_상태의_발주를_확정하면_상태가_변경되고_다운스트림_연동을_위한_발주_확정_이벤트가_발행된다() {
        }

        @Test
        void 발주_품목이_단_1개도_존재하지_않는_공백_전표를_확정하려고_하면_예외가_발생한다() {
        }

        @Test
        void 이미_확정되었거나_취소된_발주_전표를_다시_확정하려고_하면_예외가_발생한다() {
        }

        @Test
        void 존재하지_않는_발주_전표_ID로_확정을_요청하면_예외가_발생한다() {
        }
    }

    @Nested
    class 발주_취소 {

        @Test
        void 확정_또는_임시저장_상태의_발주는_취소가_가능하며_상태가_취소로_정상_전환된다() {
        }

        @Test
        void 이미_창고에_입고가_시작되었거나_완료된_발주_전표는_취소할_수_없고_예외가_발생한다() {
        }

        @Test
        void 이미_취소된_발주_전표에_대해_중복으로_취소를_요청하면_예외가_발생한다() {
        }
    }

}
