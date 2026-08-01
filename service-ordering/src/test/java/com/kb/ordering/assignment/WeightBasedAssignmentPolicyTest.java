package com.kb.ordering.assignment;

import com.kb.ordering.assignment.domain.exception.NoAssignableWarehouseException;
import com.kb.ordering.assignment.domain.exception.RoutingFailedException;
import com.kb.ordering.assignment.domain.model.DemandLine;
import com.kb.ordering.assignment.domain.model.ProductStock;
import com.kb.ordering.assignment.domain.model.WarehouseCandidate;
import com.kb.ordering.assignment.domain.service.DrivingDistanceProvider;
import com.kb.ordering.assignment.domain.service.HaversineDistanceCalculator;
import com.kb.ordering.assignment.domain.service.WeightBasedAssignmentPolicy;
import com.kb.ordering.global.geocoding.GeoCoordinate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightBasedAssignmentPolicyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 3);
    private static final Long PRODUCT_A = 100L;

    /** 배송지: 서울시청 */
    private static final GeoCoordinate DESTINATION = GeoCoordinate.of(37.5665, 126.9780);
    /** 서울 근교(김포) — 배송지에서 가까움 */
    private static final GeoCoordinate NEAR = GeoCoordinate.of(37.6152, 126.7159);
    /** 대전 — 중간 거리 */
    private static final GeoCoordinate MID = GeoCoordinate.of(36.3504, 127.3845);
    /** 부산 — 먼 거리 */
    private static final GeoCoordinate FAR = GeoCoordinate.of(35.1798, 129.0750);

    private final WeightBasedAssignmentPolicy policy = WeightBasedAssignmentPolicy.withDefaults();

    /** 주행거리 = 하버사인 × 1.3 근사 (실도로 우회 계수) */
    private final DrivingDistanceProvider roadFactorProvider =
            (origin, dest) -> Math.round(HaversineDistanceCalculator.distanceMeters(origin, dest) * 1.3);

    @Nested
    class 전량_충족_필터 {

        @Test
        void 발주_품목_전량을_충족하지_못하는_창고는_가까워도_배정_대상에서_제외된다() {
            WarehouseCandidate nearButShort = candidate(1L, NEAR, stock(50, days(100)));
            WarehouseCandidate farButEnough = candidate(2L, FAR, stock(100, days(100)));

            WarehouseCandidate selected = policy.assign(DESTINATION,
                    List.of(nearButShort, farButEnough),
                    List.of(new DemandLine(PRODUCT_A, 100)), TODAY, roadFactorProvider);

            assertThat(selected.getWarehouseId()).isEqualTo(2L);
        }

        @Test
        void 전량_충족_가능한_창고가_하나도_없으면_예외를_던진다() {
            WarehouseCandidate w1 = candidate(1L, NEAR, stock(10, days(100)));
            WarehouseCandidate w2 = candidate(2L, MID, stock(20, days(100)));

            assertThatThrownBy(() -> policy.assign(DESTINATION, List.of(w1, w2),
                    List.of(new DemandLine(PRODUCT_A, 100)), TODAY, roadFactorProvider))
                    .isInstanceOf(NoAssignableWarehouseException.class);
        }

        @Test
        void 후보_창고가_아예_없으면_예외를_던진다() {
            assertThatThrownBy(() -> policy.assign(DESTINATION, List.of(),
                    List.of(new DemandLine(PRODUCT_A, 10)), TODAY, roadFactorProvider))
                    .isInstanceOf(NoAssignableWarehouseException.class);
        }
    }

    @Nested
    class 거리_가중치 {

        @Test
        void 재고_조건이_동일하면_배송지에서_가까운_창고가_선택된다() {
            WarehouseCandidate near = candidate(1L, NEAR, stock(100, days(100)));
            WarehouseCandidate mid = candidate(2L, MID, stock(100, days(100)));
            WarehouseCandidate far = candidate(3L, FAR, stock(100, days(100)));

            WarehouseCandidate selected = policy.assign(DESTINATION, List.of(far, mid, near),
                    List.of(new DemandLine(PRODUCT_A, 50)), TODAY, roadFactorProvider);

            assertThat(selected.getWarehouseId()).isEqualTo(1L);
        }
    }

    @Nested
    class FEFO_가중치 {

        @Test
        void 거리와_재고량이_같으면_유통기한_임박_재고를_보유한_창고가_선택된다() {
            WarehouseCandidate freshStock = candidate(1L, MID, stock(100, days(300)));
            WarehouseCandidate nearExpiry = candidate(2L, MID, stock(100, days(30)));

            WarehouseCandidate selected = policy.assign(DESTINATION, List.of(freshStock, nearExpiry),
                    List.of(new DemandLine(PRODUCT_A, 50)), TODAY, roadFactorProvider);

            assertThat(selected.getWarehouseId()).isEqualTo(2L);
        }

        @Test
        void 임박_재고_보유_창고가_충분히_가까우면_최근접_창고보다_우선될_수_있다() {
            WarehouseCandidate near = candidate(1L, NEAR, stock(100, days(300)));
            WarehouseCandidate midNearExpiry = candidate(2L, MID, stock(100, days(30)));

            WarehouseCandidate selected = policy.assign(DESTINATION, List.of(near, midNearExpiry),
                    List.of(new DemandLine(PRODUCT_A, 50)), TODAY, roadFactorProvider);

            assertThat(selected.getWarehouseId()).isEqualTo(1L);
        }
    }

    @Nested
    class 재고_여유율_가중치 {

        @Test
        void 거리와_유통기한이_같으면_재고가_깊은_창고가_선택된다() {
            WarehouseCandidate shallow = candidate(1L, MID, stock(60, days(100)));
            WarehouseCandidate deep = candidate(2L, MID, stock(500, days(100)));

            WarehouseCandidate selected = policy.assign(DESTINATION, List.of(shallow, deep),
                    List.of(new DemandLine(PRODUCT_A, 50)), TODAY, roadFactorProvider);

            assertThat(selected.getWarehouseId()).isEqualTo(2L);
        }
    }

    @Nested
    class 하버사인_1차_필터 {

        @Test
        void 직선거리_상위_N개_창고에_대해서만_주행거리_API를_호출한다() {
            List<WarehouseCandidate> candidates = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                candidates.add(candidate((long) i,
                        GeoCoordinate.of(37.5665 - i * 0.05, 126.9780 + i * 0.05),
                        stock(1000, days(100))));
            }
            AtomicInteger callCount = new AtomicInteger();
            DrivingDistanceProvider countingProvider = (origin, dest) -> {
                callCount.incrementAndGet();
                return HaversineDistanceCalculator.distanceMeters(origin, dest);
            };

            policy.assign(DESTINATION, candidates,
                    List.of(new DemandLine(PRODUCT_A, 10)), TODAY, countingProvider);

            assertThat(callCount.get()).isEqualTo(5); // 기본 shortlistSize
        }
    }

    @Nested
    class 주행거리_폴백 {

        @Test
        void 주행거리_산정이_실패하면_하버사인_거리로_폴백해_배정에_성공한다() {
            WarehouseCandidate near = candidate(1L, NEAR, stock(100, days(100)));
            WarehouseCandidate far = candidate(2L, FAR, stock(100, days(100)));
            DrivingDistanceProvider failingProvider = (origin, dest) -> {
                throw new RoutingFailedException();
            };

            WarehouseCandidate selected = policy.assign(DESTINATION, List.of(near, far),
                    List.of(new DemandLine(PRODUCT_A, 50)), TODAY, failingProvider);

            assertThat(selected.getWarehouseId()).isEqualTo(1L);
        }
    }

    @Nested
    class 결정성 {

        @Test
        void 모든_조건이_동일하면_창고_ID가_낮은_창고가_선택된다() {
            WarehouseCandidate w7 = candidate(7L, MID, stock(100, days(100)));
            WarehouseCandidate w3 = candidate(3L, MID, stock(100, days(100)));

            WarehouseCandidate selected = policy.assign(DESTINATION, List.of(w7, w3),
                    List.of(new DemandLine(PRODUCT_A, 50)), TODAY, roadFactorProvider);

            assertThat(selected.getWarehouseId()).isEqualTo(3L);
        }
    }

    @Nested
    class 다량_마스터_데이터_시나리오 {

        @Test
        void 창고_500개_환경에서도_가깝고_임박_재고를_보유한_창고를_결정적으로_선택한다() {
            List<WarehouseCandidate> candidates = new ArrayList<>();
            for (int i = 1; i <= 500; i++) {
                candidates.add(candidate((long) i,
                        GeoCoordinate.of(33.0 + (i % 100) * 0.05, 125.0 + (i / 100) * 0.8),
                        stock(200 + i, days(200 + i % 150))));
            }
            WarehouseCandidate expected = candidate(999L,
                    GeoCoordinate.of(37.5700, 126.9800), stock(1000, days(10)));
            candidates.add(expected);

            WarehouseCandidate first = policy.assign(DESTINATION, candidates,
                    List.of(new DemandLine(PRODUCT_A, 100)), TODAY, roadFactorProvider);
            WarehouseCandidate second = policy.assign(DESTINATION, candidates,
                    List.of(new DemandLine(PRODUCT_A, 100)), TODAY, roadFactorProvider);

            assertThat(first.getWarehouseId()).isEqualTo(999L);
            assertThat(second.getWarehouseId()).isEqualTo(999L);
        }
    }

    // ── Fixtures ──

    private static WarehouseCandidate candidate(Long id, GeoCoordinate coordinate, ProductStock stock) {
        return WarehouseCandidate.of(id, coordinate, Map.of(PRODUCT_A, stock));
    }

    private static ProductStock stock(int quantity, LocalDate expiry) {
        return new ProductStock(quantity, expiry);
    }

    private static LocalDate days(int daysFromToday) {
        return TODAY.plusDays(daysFromToday);
    }
}
