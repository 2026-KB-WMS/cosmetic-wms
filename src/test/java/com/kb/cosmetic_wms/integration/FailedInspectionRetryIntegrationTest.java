package com.kb.cosmetic_wms.integration;

import com.kb.cosmetic_wms.inspection.application.port.in.FailedInspectionResult;
import com.kb.cosmetic_wms.inspection.application.port.in.FindFailedInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.RecordInspectionFailureCommand;
import com.kb.cosmetic_wms.inspection.application.port.in.RecordInspectionFailureUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.ResolveFailedInspectionUseCase;
import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.exception.FailedInspectionNotPendingException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionErrorCode;
import com.kb.cosmetic_wms.support.MySqlContainerSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = "spring.datasource.hikari.connection-init-sql=SET FOREIGN_KEY_CHECKS=0")
class FailedInspectionRetryIntegrationTest extends MySqlContainerSupport {

    private static final long INBOUND_ID = 9001L;
    private static final long LINE_ID = 9101L;
    private static final long PRODUCT_ID = 9201L;
    private static final long WAREHOUSE_ID = 9301L;
    private static final String LOT_NUMBER = "RETRY-LOT-1";

    @Autowired
    private RecordInspectionFailureUseCase recordInspectionFailureUseCase;

    @Autowired
    private FindFailedInspectionUseCase findFailedInspectionUseCase;

    @Autowired
    private ResolveFailedInspectionUseCase resolveFailedInspectionUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        cleanUp();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        jdbcTemplate.update("DELETE FROM quality_inspection WHERE source_id >= ? AND source_id < ?", LINE_ID, LINE_ID + 10);
        jdbcTemplate.update("DELETE FROM failed_inspection_event WHERE inbound_id = ?", INBOUND_ID);
        jdbcTemplate.update("DELETE FROM lot WHERE inbound_id = ?", INBOUND_ID);
    }

    private Long recordFailure(long lineId) {
        recordInspectionFailureUseCase.record(new RecordInspectionFailureCommand(
                INBOUND_ID, lineId, PRODUCT_ID, WAREHOUSE_ID, 50, LOT_NUMBER, LocalDate.of(2027, 1, 1),
                "최초 실패"));
        return jdbcTemplate.queryForObject(
                "SELECT failed_event_id FROM failed_inspection_event WHERE line_id = ?", Long.class, lineId);
    }

    private void insertLot() {
        jdbcTemplate.update("""
                INSERT INTO lot (inbound_id, manufacturer_lot_no, manufacturing_date, expiration_date, status,
                                 product_id, created_by, created_at)
                VALUES (?, ?, '2026-01-01', '2027-01-01', 'AVAILABLE', ?, 0, NOW())
                """, INBOUND_ID, LOT_NUMBER, PRODUCT_ID);
    }

    private int inspectionCount(long lineId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM quality_inspection WHERE source_type = 'INBOUND' AND source_id = ?",
                Integer.class, lineId);
    }

    @Test
    void 기록된_실패_건은_처리_대기_상태로_조회된다() {
        Long failedId = recordFailure(LINE_ID);

        List<FailedInspectionResult> pending = findFailedInspectionUseCase.findByStatus(FailedEventStatus.PENDING);

        assertThat(pending).extracting(FailedInspectionResult::id).contains(failedId);
        FailedInspectionResult result = pending.stream().filter(r -> r.id().equals(failedId)).findFirst().orElseThrow();
        assertThat(result.status()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(result.retryCount()).isZero();
        assertThat(result.errorMessage()).isEqualTo("최초 실패");
    }

    @Test
    void 원인이_해소되지_않으면_재처리가_실패해도_대기_상태로_시도_횟수와_사유가_DB에_남는다() {
        // given — 로트가 아직 없어 전표를 만들 수 없다
        Long failedId = recordFailure(LINE_ID);

        // when
        FailedInspectionResult result = resolveFailedInspectionUseCase.retry(failedId);

        // then — 별도 트랜잭션의 롤백이 실패 기록까지 되돌리지 않는다
        assertThat(result.status()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(result.retryCount()).isEqualTo(1);
        assertThat(result.errorMessage()).isEqualTo(InspectionErrorCode.INSPECTION_LOT_NOT_FOUND.getMessage());

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT status, retry_count, resolved_at FROM failed_inspection_event WHERE failed_event_id = ?", failedId);
        assertThat(row.get("status")).isEqualTo("PENDING");
        assertThat(((Number) row.get("retry_count")).intValue()).isEqualTo(1);
        assertThat(row.get("resolved_at")).isNull();
        assertThat(inspectionCount(LINE_ID)).isZero();
    }

    @Test
    void 원인을_해소한_뒤_재처리하면_검사_전표가_만들어지고_해결_처리된다() {
        // given
        Long failedId = recordFailure(LINE_ID);
        resolveFailedInspectionUseCase.retry(failedId);
        insertLot();

        // when
        FailedInspectionResult result = resolveFailedInspectionUseCase.retry(failedId);

        // then
        assertThat(result.status()).isEqualTo(FailedEventStatus.RESOLVED);
        assertThat(result.retryCount()).isEqualTo(2);
        assertThat(inspectionCount(LINE_ID)).isEqualTo(1);

        Map<String, Object> inspection = jdbcTemplate.queryForMap(
                "SELECT status, inspection_quantity, warehouse_id FROM quality_inspection WHERE source_id = ?", LINE_ID);
        assertThat(inspection.get("status")).isEqualTo("WAITING");
        assertThat(((Number) inspection.get("inspection_quantity")).intValue()).isEqualTo(50);
        assertThat(((Number) inspection.get("warehouse_id")).longValue()).isEqualTo(WAREHOUSE_ID);

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT status, resolved_at FROM failed_inspection_event WHERE failed_event_id = ?", failedId);
        assertThat(row.get("status")).isEqualTo("RESOLVED");
        assertThat(row.get("resolved_at")).isNotNull();
    }

    @Test
    void 해결된_건을_다시_재처리하면_예외를_던지고_전표를_중복_생성하지_않는다() {
        // given
        insertLot();
        Long failedId = recordFailure(LINE_ID);
        resolveFailedInspectionUseCase.retry(failedId);

        // when & then
        assertThatThrownBy(() -> resolveFailedInspectionUseCase.retry(failedId))
                .isInstanceOf(FailedInspectionNotPendingException.class);
        assertThat(inspectionCount(LINE_ID)).isEqualTo(1);
    }

    @Test
    void 같은_라인의_전표가_이미_있으면_중복_생성하지_않고_해결_처리한다() {
        // given — 두 번째 실패 기록이 남았지만 전표는 이미 만들어진 상황
        insertLot();
        Long first = recordFailure(LINE_ID);
        resolveFailedInspectionUseCase.retry(first);
        recordInspectionFailureUseCase.record(new RecordInspectionFailureCommand(
                INBOUND_ID, LINE_ID, PRODUCT_ID, WAREHOUSE_ID, 50, LOT_NUMBER, LocalDate.of(2027, 1, 1), "중복 기록"));
        Long duplicate = jdbcTemplate.queryForObject(
                "SELECT MAX(failed_event_id) FROM failed_inspection_event WHERE line_id = ?", Long.class, LINE_ID);

        // when
        FailedInspectionResult result = resolveFailedInspectionUseCase.retry(duplicate);

        // then
        assertThat(result.status()).isEqualTo(FailedEventStatus.RESOLVED);
        assertThat(inspectionCount(LINE_ID)).isEqualTo(1);
    }

    @Test
    void 폐기한_건은_처리_대기_목록에서_사라지고_재처리할_수_없다() {
        // given
        Long failedId = recordFailure(LINE_ID);

        // when
        FailedInspectionResult discarded = resolveFailedInspectionUseCase.discard(failedId);

        // then
        assertThat(discarded.status()).isEqualTo(FailedEventStatus.DISCARDED);
        assertThat(findFailedInspectionUseCase.findByStatus(FailedEventStatus.PENDING))
                .extracting(FailedInspectionResult::id).doesNotContain(failedId);
        assertThatThrownBy(() -> resolveFailedInspectionUseCase.retry(failedId))
                .isInstanceOf(FailedInspectionNotPendingException.class);
    }

    @Test
    void 같은_건을_동시에_재처리해도_전표는_한_번만_만들어지고_나머지는_거절된다() throws Exception {
        // given
        insertLot();
        Long failedId = recordFailure(LINE_ID);
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Callable<Boolean> task = () -> {
                ready.countDown();
                start.await();
                try {
                    resolveFailedInspectionUseCase.retry(failedId);
                    return true;
                } catch (FailedInspectionNotPendingException e) {
                    return false;
                }
            };
            futures.add(executor.submit(task));
        }

        // when
        ready.await();
        start.countDown();
        int succeeded = 0;
        for (Future<Boolean> future : futures) {
            if (future.get(30, TimeUnit.SECONDS)) {
                succeeded++;
            }
        }
        executor.shutdown();

        // then
        assertThat(succeeded).isEqualTo(1);
        assertThat(inspectionCount(LINE_ID)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT retry_count FROM failed_inspection_event WHERE failed_event_id = ?", Integer.class, failedId))
                .isEqualTo(1);
    }
}
