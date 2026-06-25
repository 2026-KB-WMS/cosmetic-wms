package com.kb.cosmetic_wms.inventory;

import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryStatusSet;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryTransaction;
import com.kb.cosmetic_wms.inventory.fixture.InventoryTransactionTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InventoryTransactionEntityTest {

    @Test
    void 재고_이력_엔티티는_올바른_식별자_ID와_상태값들로_정상_생성되어야_한다() {
        InventoryTransaction transaction = new InventoryTransactionTestBuilder().build();

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
                .hasMessage("재고 식별자(ID)는 필수입니다.");
    }

    @Test
    void 재고_이력_생성_시_트랜잭션_타입이_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new InventoryTransactionTestBuilder()
                        .transactionType(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("트랜잭션 타입은 필수입니다.");
    }

    @Test
    void 재고_이력_생성_시_작업자_식별자_ID가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new InventoryTransactionTestBuilder()
                        .memberId(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작업자 식별자(ID)는 필수입니다.");
    }

    @ParameterizedTest
    @EnumSource(value = TransactionType.class, names = {
            "INBOUND_PUTAWAY", "INBOUND_CANCEL", "ALLOCATE", "UNALLOCATE", "PICKING", "SHIP"
    })
    void 상위_전표가_필수인_타입으로_이력_생성_시_전표_ID가_누락되면_예외를_던진다(TransactionType transactionType) {
        String expectedMessage = String.format("%s 행위는 원인 전표 ID가 필수입니다.", transactionType.getDescription());

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
        InventoryTransaction transaction = new InventoryTransactionTestBuilder()
                .transactionType(transactionType)
                .referenceId(null)
                .build();

        assertThat(transaction.getReferenceId()).isNull();
    }

    @Test
    void 최초_입고_적재_시에는_이전_재고_상태셋이_null이어도_정상_생성된다() {
        InventoryTransaction transaction = new InventoryTransactionTestBuilder()
                .nullPrevStatusSet()
                .build();

        assertThat(transaction.getPrevStatusSet()).isNull();
    }

    @Test
    void 재고_조정_또는_상태_변경_시_이전_상태셋과_현재_상태셋이_모두_안전하게_기록된다() {
        InventoryStatusSet expectedPrevStatus = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, QualityStatus.NORMAL, LocStatus.STORED
        );
        InventoryStatusSet expectedCurrStatus = InventoryStatusSet.of(
                AllocStatus.ALLOCATED, QualityStatus.NORMAL, LocStatus.STORED
        );

        InventoryTransaction transaction = new InventoryTransactionTestBuilder()
                .prevStatusSet(expectedPrevStatus)
                .currStatusSet(expectedCurrStatus)
                .build();

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
                .hasMessage("트랜잭션 변동 수량은 0보다 커야 합니다.");
    }
}
