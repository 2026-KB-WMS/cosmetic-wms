package com.kb.cosmetic_wms.oms;

import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.exception.FailedAssignmentNotPendingException;
import com.kb.cosmetic_wms.oms.domain.model.FailedAssignment;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailedAssignmentEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 3, 12, 0);

    private FailedAssignment pending() {
        return FailedAssignment.record(1L, 10L, "Routing API timeout", NOW);
    }

    @Test
    void 실패를_기록하면_처리_대기_상태로_시작한다() {
        FailedAssignment failed = pending();

        assertThat(failed.getStatus()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(failed.getRetryCount()).isZero();
        assertThat(failed.getResolvedAt()).isNull();
        assertThat(failed.getFailedAt()).isEqualTo(NOW);
    }

    @Test
    void 재처리에_성공하면_해결_상태가_되고_시도_횟수와_처리_시각이_기록된다() {
        FailedAssignment failed = pending();

        failed.markResolvedByRetry(NOW.plusMinutes(5));

        assertThat(failed.getStatus()).isEqualTo(FailedEventStatus.RESOLVED);
        assertThat(failed.getRetryCount()).isEqualTo(1);
        assertThat(failed.getResolvedAt()).isEqualTo(NOW.plusMinutes(5));
    }

    @Test
    void 재처리에_실패하면_대기_상태를_유지하고_시도_횟수와_최근_오류가_갱신된다() {
        FailedAssignment failed = pending();

        failed.recordRetryFailure("전량 충족 창고 없음");

        assertThat(failed.getStatus()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(failed.getRetryCount()).isEqualTo(1);
        assertThat(failed.getErrorMessage()).isEqualTo("전량 충족 창고 없음");
    }

    @Test
    void 폐기하면_폐기_상태가_되고_시도_횟수는_늘지_않는다() {
        FailedAssignment failed = pending();

        failed.discard(NOW.plusDays(1));

        assertThat(failed.getStatus()).isEqualTo(FailedEventStatus.DISCARDED);
        assertThat(failed.getRetryCount()).isZero();
    }

    @Test
    void 처리된_건은_다시_처리하거나_폐기할_수_없다() {
        FailedAssignment resolved = pending();
        resolved.markResolvedByRetry(NOW);
        FailedAssignment discarded = pending();
        discarded.discard(NOW);

        assertThatThrownBy(() -> resolved.markResolvedByRetry(NOW)).isInstanceOf(FailedAssignmentNotPendingException.class);
        assertThatThrownBy(() -> resolved.recordRetryFailure("x")).isInstanceOf(FailedAssignmentNotPendingException.class);
        assertThatThrownBy(() -> discarded.discard(NOW)).isInstanceOf(FailedAssignmentNotPendingException.class);
        assertThatThrownBy(discarded::assertPending).isInstanceOf(FailedAssignmentNotPendingException.class);
    }
}
