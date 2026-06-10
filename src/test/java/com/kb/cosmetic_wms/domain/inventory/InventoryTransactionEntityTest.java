package com.kb.cosmetic_wms.domain.inventory;

import com.kb.cosmetic_wms.domain.inventory.constants.InventoryConstants;
import com.kb.cosmetic_wms.domain.inventory.entity.InventoryStatusSet;
import com.kb.cosmetic_wms.domain.inventory.entity.InventoryTransaction;
import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.TransactionType;
import com.kb.cosmetic_wms.domain.inventory.fixture.InventoryTransactionTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InventoryTransactionEntityTest {

    @Test
    void 재고_이력_엔티티는_올바른_식별자_ID와_상태값들로_정상_생성되어야_한다() {
        // given & when
        InventoryTransaction transaction = new InventoryTransactionTestBuilder().build();

        // then
        assertThat(transaction.getInventoryId()).isEqualTo(100L);
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.LOCATION_MOVE);
        assertThat(transaction.getTransactionQuantity()).isEqualTo(10);
        assertThat(transaction.getBalanceQuantity()).isEqualTo(100);
    }

    @Test
    void 재고_이력_생성_시_재고_식별자_ID가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new InventoryTransactionTestBuilder()
                        .inventoryId(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.INVENTORY_ID_REQUIRED_MESSAGE);
    }

    @Test
    void 재고_이력_생성_시_트랜잭션_타입이_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new InventoryTransactionTestBuilder()
                        .transactionType(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.TRANSACTION_TYPE_REQUIRED_MESSAGE);
    }

    @Test
    void 재고_이력_생성_시_작업자_식별자_ID가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new InventoryTransactionTestBuilder()
                        .memberId(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.MEMBER_ID_REQUIRED_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = TransactionType.class, names = {
            "INBOUND_PUTAWAY", "INBOUND_CANCEL", "ALLOCATE", "UNALLOCATE", "PICKING", "SHIP"
    })
    void 상위_전표가_필수인_타입으로_이력_생성_시_전표_ID가_누락되면_예외를_던진다(TransactionType transactionType) {
        // given
        String expectedMessage = String.format(
                InventoryConstants.REFERENCE_ID_REQUIRED_TEMPLATE, transactionType.getDescription());

        assertThatThrownBy(() ->
                new InventoryTransactionTestBuilder()
                        .transactionType(transactionType)
                        .referenceId(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    @ParameterizedTest
    @EnumSource(value = TransactionType.class, names = {
            "LOCATION_MOVE", "QUALITY_INSPECTING", "QUALITY_HOLD", "QUALITY_RELEASE", "DISCARD"
    })
    void 상위_전표가_선택인_타입으로_이력_생성_시_전표_ID가_없어도_정상_생성된다(TransactionType transactionType) {
        // given & when
        InventoryTransaction transaction = new InventoryTransactionTestBuilder()
                .transactionType(transactionType)
                .referenceId(null)
                .build();

        // then
        assertThat(transaction.getReferenceId()).isNull();
    }

    @Test
    void 최초_입고_적재_시에는_이전_재고_상태셋이_null이어도_정상_생성된다() {
        // given & when
        InventoryTransaction transaction = new InventoryTransactionTestBuilder()
                .nullPrevStatusSet()
                .build();

        // then
        assertThat(transaction.getPrevStatusSet()).isNull();
    }

    @Test
    void 재고_조정_또는_상태_변경_시_이전_상태셋과_현재_상태셋이_모두_안전하게_기록된다() {
        // given
        InventoryStatusSet expectedPrevStatus = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, QualityStatus.NORMAL, LocStatus.STORED
        );
        InventoryStatusSet expectedCurrStatus = InventoryStatusSet.of(
                AllocStatus.ALLOCATED, QualityStatus.NORMAL, LocStatus.STORED
        );

        // when
        InventoryTransaction transaction = new InventoryTransactionTestBuilder()
                .prevStatusSet(expectedPrevStatus)
                .currStatusSet(expectedCurrStatus)
                .build();

        // then
        assertThat(transaction.getPrevStatusSet()).isNotNull();
        assertThat(transaction.getPrevStatusSet().allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);
        assertThat(transaction.getPrevStatusSet().qualityStatus()).isEqualTo(QualityStatus.NORMAL);

        assertThat(transaction.getCurrStatusSet()).isNotNull();
        assertThat(transaction.getCurrStatusSet().allocStatus()).isEqualTo(AllocStatus.ALLOCATED);
        assertThat(transaction.getCurrStatusSet().qualityStatus()).isEqualTo(QualityStatus.NORMAL);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5, -100})
    void 재고_이력_생성_시_변동_수량이_0_이하거나_음수이면_예외를_던진다(int invalidQuantity) {
        assertThatThrownBy(() ->
                new InventoryTransactionTestBuilder()
                        .transactionQuantity(invalidQuantity)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InventoryConstants.INVALID_TRANSACTION_QTY_MESSAGE);
    }
}
