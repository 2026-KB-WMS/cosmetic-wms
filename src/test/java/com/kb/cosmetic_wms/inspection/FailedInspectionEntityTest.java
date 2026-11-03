package com.kb.cosmetic_wms.inspection;

import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.exception.FailedInspectionNotPendingException;
import com.kb.cosmetic_wms.inspection.domain.model.FailedInspection;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailedInspectionEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 3, 12, 0);

    private FailedInspection pending() {
        return FailedInspection.record(10L, 20L, 30L, 40L, 50, "LOT0001", LocalDate.of(2027, 1, 1), "DB 연결 실패", NOW);
    }

    @Test
    void 실패를_기록하면_처리_대기_상태로_시작한다() {
        FailedInspection failed = pending();

        assertThat(failed.getStatus()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(failed.getRetryCount()).isZero();
        assertThat(failed.getResolvedAt()).isNull();
        assertThat(failed.getFailedAt()).isEqualTo(NOW);
        assertThat(failed.getErrorMessage()).isEqualTo("DB 연결 실패");
    }

    @Test
    void 재처리에_성공하면_해결_상태가_되고_시도_횟수와_처리_시각이_기록된다() {
        FailedInspection failed = pending();

        failed.markResolvedByRetry(NOW.plusHours(1));

        assertThat(failed.getStatus()).isEqualTo(FailedEventStatus.RESOLVED);
        assertThat(failed.getRetryCount()).isEqualTo(1);
        assertThat(failed.getResolvedAt()).isEqualTo(NOW.plusHours(1));
    }

    @Test
    void 재처리에_실패하면_대기_상태를_유지하고_시도_횟수와_최근_오류가_갱신된다() {
        FailedInspection failed = pending();

        failed.recordRetryFailure("로트를 찾을 수 없습니다.");
        failed.recordRetryFailure("다시 실패");

        assertThat(failed.getStatus()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(failed.getRetryCount()).isEqualTo(2);
        assertThat(failed.getErrorMessage()).isEqualTo("다시 실패");
        assertThat(failed.getResolvedAt()).isNull();
    }

    @Test
    void 폐기하면_폐기_상태가_되고_시도_횟수는_늘지_않는다() {
        FailedInspection failed = pending();

        failed.discard(NOW.plusHours(2));

        assertThat(failed.getStatus()).isEqualTo(FailedEventStatus.DISCARDED);
        assertThat(failed.getRetryCount()).isZero();
        assertThat(failed.getResolvedAt()).isEqualTo(NOW.plusHours(2));
    }

    @Test
    void 이미_해결된_건은_다시_처리할_수_없다() {
        FailedInspection failed = pending();
        failed.markResolvedByRetry(NOW);

        assertThatThrownBy(() -> failed.markResolvedByRetry(NOW)).isInstanceOf(FailedInspectionNotPendingException.class);
        assertThatThrownBy(() -> failed.recordRetryFailure("x")).isInstanceOf(FailedInspectionNotPendingException.class);
        assertThatThrownBy(() -> failed.discard(NOW)).isInstanceOf(FailedInspectionNotPendingException.class);
    }

    @Test
    void 이미_폐기된_건은_재처리할_수_없다() {
        FailedInspection failed = pending();
        failed.discard(NOW);

        assertThatThrownBy(failed::assertPending).isInstanceOf(FailedInspectionNotPendingException.class);
    }
}
